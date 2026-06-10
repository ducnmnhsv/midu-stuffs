package com.techx.tradex.realtime.services;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.utils.MonitorThreadHandler;
import com.difisoft.market.common.utils.ThreadHandler;
import com.difisoft.market.model.v2.db.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.model.MonitorStatistic;
import com.techx.tradex.realtime.model.request.ExtraQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


@Service
@Slf4j
@Lazy(value = false)
public class MonitorService extends MonitorThreadHandler<Object, ThreadHandler<Object>> {
    public static final Function<Object, String> getCode = item -> {
        if (item instanceof SymbolQuote) {
            return ((SymbolQuote) item).getCode();
        } else if (item instanceof BidOffer) {
            return ((BidOffer) item).getCode();
        } else if (item instanceof ExtraQuote) {
            return ((ExtraQuote) item).getCode();
        } else if (item instanceof Advertised) {
            return ((Advertised) item).getCode();
        } else if (item instanceof BidOfferOddLot) {
            return ((BidOfferOddLot) item).getCode();
        } else if (item instanceof DealNotice) {
            return ((DealNotice) item).getCode();
        } else if (item instanceof IndexStockList) {
            return ((IndexStockList) item).getIndexCode();
        }
        return null;
    };
    private final AppConf appConf;
    private final QuoteService quoteService;
    private final CacheService cacheService;
    private final MarketRedisDao marketRedisDao;
    private final MonitorStatistic<SymbolQuote> monitorQuoteService;
    private final MonitorStatistic<BidOffer> monitorBidOfferService;
    private final MonitorStatistic<BidOfferOddLot> monitorBidOfferOddLotService;
    private final MonitorStatistic<ExtraQuote> monitorExtraQuoteService;
    private final MonitorStatistic<Advertised> monitorAdvertisedService;
    private final MonitorStatistic<DealNotice> monitorDealNoticeService;
    private final IndexStockService indexStockService;

    @Autowired
    public MonitorService(
            ObjectMapper objectMapper,
            AppConf appConf,
            CacheService cacheService,
            MarketRedisDao marketRedisDao,
            QuoteService quoteService,
            BidOfferService bidOfferService,
            ExtraQuoteService extraQuoteService,
            AdvertisedService advertisedService,
            DealNoticeService dealNoticeService,
            IndexStockService indexStockService) {
        super(objectMapper, getCode, appConf.getMonitorFile());
        this.appConf = appConf;
        this.quoteService = quoteService;
        this.cacheService = cacheService;
        this.marketRedisDao = marketRedisDao;
        this.monitorQuoteService = new MonitorStatistic<>("QuoteService", quoteService::updateQuote, this::logError);
        this.monitorBidOfferService = new MonitorStatistic<>("BidOfferService", bidOfferService::updateBidOffer, this::logError);
        this.monitorBidOfferOddLotService = new MonitorStatistic<>("BidOfferOddLotService", bidOfferService::updateBidOfferOddLot, this::logError);
        this.monitorExtraQuoteService = new MonitorStatistic<>("ExtraQuoteService", extraQuoteService::updateExtraQuote, this::logError);
        this.monitorAdvertisedService = new MonitorStatistic<>("AdvertisedService", advertisedService::updateAdvertised, this::logError);
        this.monitorDealNoticeService = new MonitorStatistic<>("DealNoticeService", dealNoticeService::updateDealNotice, this::logError);
        this.indexStockService = indexStockService;
    }

    protected void logError(Throwable err) {
        log.error("fail to handler an item", err);
    }

    protected void handler(Object item) {
        try {
            if (item instanceof SymbolQuote) {
                this.monitorQuoteService.process((SymbolQuote) item);
            } else if (item instanceof BidOffer) {
                this.monitorBidOfferService.process((BidOffer) item);
            } else if (item instanceof ExtraQuote) {
                this.monitorExtraQuoteService.process((ExtraQuote) item);
            } else if (item instanceof Advertised) {
                this.monitorAdvertisedService.process((Advertised) item);
            } else if (item instanceof BidOfferOddLot) {
                this.monitorBidOfferOddLotService.process((BidOfferOddLot) item);
            } else if (item instanceof DealNotice) {
                this.monitorDealNoticeService.process((DealNotice) item);
            } else if (item instanceof IndexStockList) {
                this.indexStockService.updateIndexList((IndexStockList) item);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ThreadHandler<Object> createThread(int index) {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        return new ThreadHandler<>(index, "realtimeHandler", queue, this::handler, quoteService::handleWrongOrderQuote, appConf.getQuotePartitionIntervalSecond());
    }

    public void init() {
        log.info("prepare thread");
        this.createAllThread(appConf.getNoOfThreadHandler());
        this.divideSymbolToThread(cacheService.getMapSymbolInfo().keySet());
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                monitorQuoteService.log(log);
                monitorBidOfferService.log(log);
                monitorBidOfferOddLotService.log(log);
                monitorExtraQuoteService.log(log);
                monitorAdvertisedService.log(log);
                monitorDealNoticeService.log(log);
            }
        }, 60000, 300000);
    }

    public CompletableFuture<Boolean> doRecoverQuoteMinute(String code, RequestContext<String> ctx) {
        CompletableFuture<Boolean> fut = new CompletableFuture<>();
        this.registerExecution(code, t -> {
            try {
                quoteService.recoverMinute(ctx.getId(), code);
                fut.complete(true);
            } catch (Exception e) {
                log.error("{} fail to recover minute {}", ctx.getId(), code, e);
                fut.complete(false);
            }
        });
        return fut;
    }

    public CompletableFuture<Boolean> doMergeWrongOrderQuote(String code, RequestContext<String> ctx) {
        CompletableFuture<Boolean> fut = new CompletableFuture<>();
        this.registerExecution(code, t -> {
            try {
                quoteService.mergeWrongOrderQuotes(code, ctx);
                fut.complete(true);
            } catch (Exception e) {
                log.error("{} fail to recover minute {}", ctx.getId(), code, e);
                fut.complete(false);
            }
        });
        return fut;
    }

    public CompletableFuture<Integer> recoverAllMinute(Integer threshHold, RequestContext<Integer> ctx) {
        int maximumPoints = threshHold == null ? 360 : threshHold;
        AtomicInteger finished = new AtomicInteger();
        AtomicInteger success = new AtomicInteger();
        CompletableFuture<Integer> fut = new CompletableFuture<>();
        Set <String> codes = new HashSet<>(this.cacheService.getMapSymbolInfo().keySet());
        codes.forEach(code -> {
            Long size = marketRedisDao.symbolQuoteMinuteSize(code);
            if (size != null && size > maximumPoints) {
                this.registerExecution(code, t -> {
                    try {
                        quoteService.recoverMinute(ctx.getId(), code);
                        success.incrementAndGet();
                    } catch (Exception e) {
                        log.error("{} fail to recover minute {}", ctx.getId(), code, e);
                    }
                    if (finished.incrementAndGet() >= codes.size()) {
                        fut.complete(success.get());
                    }
                });
            }
        });
        return fut;
    }
}
