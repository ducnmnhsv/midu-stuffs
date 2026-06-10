package com.difisoft.marketcollector.services;

import com.difisoft.file.FileService;
import com.difisoft.htsconnection.socket.nonblocking.BaseHtsConnectionHandler;
import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.MarketInit;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.IndexStockListRepository;
import com.difisoft.market.common.repository.SymbolDailyRepository;
import com.difisoft.market.common.repository.SymbolInfoRepository;
import com.difisoft.market.common.repository.SymbolInfoRollerRepository;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.constants.Constants;
import com.difisoft.marketcollector.model.db.Symbol;
import com.difisoft.marketcollector.model.lotte.api.*;
import com.difisoft.marketcollector.utils.CompletableUtil;
import com.difisoft.model.kafka.Message;
import com.difisoft.model.utils.CompletablePool;
import com.difisoft.model.utils.DefaultUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class HtsSymbolInfoService implements ISymbolInfoService {

    private final MarketInit marketInit;
    private final AppConf appConf;
    private final RequestSender requestSender;
    private final MarketRedisDao marketRedisDao;
    private final DownloadSymbolListService downloadSymbolListService;
    private final DownloadInfoService downloadInfoService;
    private final CacheService cacheService;
    private final DownloadAccountService downloadAccountService;
    private final RealTimeDataListenerService realTimeDataListenerService;
    private final CoordinatorService coordinatorService;
    private final HolidayService holidayService;

    public HtsSymbolInfoService(SymbolInfoRepository symbolInfoRepo,
                                     AppConf appConf, RequestSender requestSender,
                                     DownloadSymbolListService downloadSymbolListService,
                                     DownloadInfoService downloadInfoService,
                                     CacheService cacheService,
                                     MarketRedisDao marketRedisDao,
                                     IndexStockListRepository indexStockListRepository,
                                     ObjectMapper objectMapper,
                                     SymbolInfoRollerRepository symbolInfoRollerRepository,
                                     SymbolDailyRepository symbolDailyRepository,
                                     DownloadAccountService downloadAccountService,
                                     RealTimeDataListenerService realTimeDataListenerService,
                                     FileService fileService,
                                     CoordinatorService coordinatorService,
                                     HolidayService holidayService
    ) {
        this.appConf = appConf;
        this.downloadAccountService = downloadAccountService;
        this.requestSender = requestSender;
        this.cacheService = cacheService;
        this.downloadSymbolListService = downloadSymbolListService;
        this.downloadInfoService = downloadInfoService;
        this.realTimeDataListenerService = realTimeDataListenerService;
        this.coordinatorService = coordinatorService;
        this.holidayService = holidayService;
        this.marketRedisDao = marketRedisDao;
        this.marketInit = new MarketInit(
                marketRedisDao.getRedisDao(),
                symbolInfoRepo,
                indexStockListRepository,
                this,
                appConf.getMarketConf(),
                objectMapper,
                fileService,
                symbolInfoRollerRepository,
                symbolDailyRepository
        );
    }

    @Override
    public void send(String topic, String uri, Object data) {
        Message<Object> msg = new Message<>();
        msg.setData(data);
        msg.setUri("Update");
        try {
            this.requestSender.sendMessageNoResponse(topic, uri, data);
        } catch (IOException e) {
            log.error("fail to send message {} {} {}", topic, uri, data, e);
        }
    }

    public Object downloadSymbolFromRequest(Object ignoredRequest, RequestContext<Object> ctx) {
        return this.downloadSymbol(ctx.getId());
    }

    public Object forceDownloadSymbolFromRequest(Object ignoredRequest, RequestContext<Object> ctx) {
        this.realTimeDataListenerService.stop();
        try {
            return this.downloadSymbol(ctx.getId());
        } finally {
            this.realTimeDataListenerService.run();
        }
    }

    public CompletableFuture<Void> downloadSymbol(String id) {
        String coordinatorKey = appConf.getServiceName() + "_" + "downloadSymbolJob";
        if (!appConf.isEnableMultipleInstance() || coordinatorService.acquire(coordinatorKey, appConf.getNodeId(), 30000) != null) {
            try {
                return this.downloadSymbol(id, 0);
            } finally {
                coordinatorService.release(coordinatorKey);
            }
        } else {
            log.info("{} this will not start download symbol info service do not ", id);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void recoverFromRedis(CompletablePool<SymbolInfo> pool, List<SymbolInfo> allSymbols) {
        pool.getMapResult().forEach((key, value) -> {
            if (value.getRight() != null) {
                SymbolInfo s = marketRedisDao.getSymbolInfo(key);
                if (s != null) {
                    log.warn("recover symbol {} info from redis {}", s.getCode(), s);
                    allSymbols.add(s);
                }
            }
        });
    }

    private CompletableFuture<Void> downloadSymbol(String id, int index) {
        log.info("{} downloadSymbol {} times", id, index);
        if (index >= Constants.MAX_RETRY) {
            log.error("{} retry getTblFiles exceeded {} times Stop", id, Constants.MAX_RETRY);
            return CompletableUtil.exception(new RuntimeException("retry getTblFiles exceeded {} times Stop"));
        }
        try {
            log.info("========================= {} START DOWNLOAD SYMBOL INFO {}============================", id, index);
            if (holidayService.isHoliday()) {
                log.info("========== {} TODAY IS HOLIDAY OR WEEKEND - END getTblFiles =======", id);
                return CompletableFuture.completedFuture(null);
            }

            log.info("{} download tbl from hts server master", id);
            Map<String, Symbol> mapSymbol = downloadSymbolListService.downloadFuture(false).join();

            List<Symbol> stockList = new ArrayList<>();
            List<Symbol> indexList = new ArrayList<>();
            List<Symbol> futuresList = new ArrayList<>();
            List<Symbol> bondList = new ArrayList<>();
            List<Symbol> cwList = new ArrayList<>();
            List<Symbol> allSymbolList = new ArrayList<>();
            mapSymbol.forEach((code, symbol) -> {
                if (!symbol.isValid()) {
                    log.warn("{} instrument data is missing {}", id, symbol);
                    return;
                }

                boolean adding = true;
                if (symbol.getType().equals(SymbolTypeEnum.INDEX)) {
                    indexList.add(symbol);
                } else if (symbol.getType().equals(SymbolTypeEnum.STOCK)) {
                    stockList.add(symbol);
                } else if (symbol.getType().equals(SymbolTypeEnum.FUTURES)) {
                    futuresList.add(symbol);
                } else if (symbol.getType().equals(SymbolTypeEnum.BOND)) {
                    if (appConf.isEnableBond()) {
                        bondList.add(symbol);
                    } else {
                        adding = false;
                    }
                } else if (symbol.getType().equals(SymbolTypeEnum.CW)) {
                    cwList.add(symbol);
                }
                if (adding) {
                    allSymbolList.add(symbol);
                }
            });
            log.info("{} stockList: {} _ indexList: {} _ futuresList: {} _ cwList: {} _ total: {}", id,
                    stockList.size(), indexList.size(), futuresList.size(), cwList.size(), allSymbolList.size());

            DownloadAccountService.ConnectionController connectionController = downloadAccountService.getConnection();
            BaseHtsConnectionHandler connection = connectionController.getConnectionHandler();
            List<SymbolInfo> allSymbols = new ArrayList<>();
            CompletablePool<SymbolInfo> pool = downloadInfoService.downloadStockInfo(connection, stockList).join();
            if (pool != null) {
                allSymbols = pool.getFutureResults().getSuccess();
                recoverFromRedis(pool, allSymbols);
            }
            log.info("{} symbol size {}", id, allSymbols.size());
            pool = downloadInfoService.downloadFuturesInfo(connection, futuresList).join();
            if (pool != null) {
                allSymbols.addAll(pool.getFutureResults().getSuccess());
                recoverFromRedis(pool, allSymbols);
            }
            log.info("{} symbol size {}", id, allSymbols.size());
            pool = downloadInfoService.downloadCWInfo(connection, cwList).join();
            if (pool != null) {
                allSymbols.addAll(pool.getFutureResults().getSuccess());
                recoverFromRedis(pool, allSymbols);
            }
            log.info("{} symbol size {}", id, allSymbols.size());
            pool = downloadInfoService.downloadIndexInfo(connection, indexList).join();
            if (pool != null) {
                allSymbols.addAll(pool.getFutureResults().getSuccess());
                recoverFromRedis(pool, allSymbols);
            }
            log.info("{} symbol size {}", id, allSymbols.size());
            log.info("{} release download info connection", id);
            connectionController.release();
            if (allSymbols.size() < appConf.getInitThresholdSize()) {
                throw new IllegalStateException(id + " Not enough symbols:" + allSymbols.size() + " while need at least: " + appConf.getInitThresholdSize());
            }
            if (appConf.isEnableInitMarket()) {
                this.marketInit.init(allSymbols);
            } else {
                String groupId = String.format("%d-%s", System.currentTimeMillis(), id);
                this.marketInit.sendSymbolInfoUpdate(groupId, appConf.getTopics().getSymbolInfoUpdate(), allSymbols, command -> command.setCleanAll(true));
                log.info("========================= {} FINISH DOWNLOAD SYMBOL INFO {}============================", id, index);
            }
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("{} error while downloadSymbol", id, e);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ignore) {
            }
            return this.downloadSymbol(id, index + 1);
        }
    }
}
