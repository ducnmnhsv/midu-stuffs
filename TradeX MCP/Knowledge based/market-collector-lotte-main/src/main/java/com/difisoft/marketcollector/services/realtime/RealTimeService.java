package com.difisoft.marketcollector.services.realtime;

import com.difisoft.htsconnection.socket.message.AutoRcv;
import com.difisoft.htsconnection.socket.nonblocking.BaseHtsConnectionHandler;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.model.constant.MarketTypeEnum;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.db.Symbol;
import com.difisoft.marketcollector.model.realtime.TransformData;
import com.difisoft.marketcollector.services.*;
import com.difisoft.marketcollector.ws.WsConnectionThread;
import com.difisoft.model.utils.LoopLinkedList;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RealTimeService {
    static final String ALL_EXCHANGE = "ALL_EXCHANGE";
    final CacheService cacheService;
    final AppConf appConf;
    final MarketRedisDao marketRedisDao;
    final DownloadSymbolListService downloadSymbolListService;
    final MonitorService monitorService;
    final HtsConnectionService htsConnectionService;
    private Collection<Symbol> symbols;
    private List<SymbolInfo> securitiesInfoList;
    final Calendar from = Calendar.getInstance();
    final Calendar to = Calendar.getInstance();
    volatile boolean isRunning = false;
    final List<RealTimeConnectionHandler> connections = new ArrayList<>();
    final ObjectMapper objectMapper;
    final Map<String, Class<? extends TransformData<AutoRcv>>> destClassMap = new HashMap<>();
    Map<String, AppConf.SendOut> sendOutMap = new HashMap<>();


    @Autowired
    public RealTimeService(
            AppConf appConf
            , CacheService cacheService
            , MonitorService monitorService
            , MarketRedisDao marketRedisDao
            , HtsConnectionService htsConnectionService
            , DownloadSymbolListService downloadSymbolListService
            , ObjectMapper objectMapper
    ) {
        this.cacheService = cacheService;
        this.appConf = appConf;
        this.monitorService = monitorService;
        this.marketRedisDao = marketRedisDao;
        this.htsConnectionService = htsConnectionService;
        this.downloadSymbolListService = downloadSymbolListService;
        this.objectMapper = objectMapper;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        try {
            from.setTime(sdf.parse(appConf.getRealtime().getWorkingTime().getFrom()));
            to.setTime(sdf.parse(appConf.getRealtime().getWorkingTime().getTo()));
        } catch (Exception e) {
            log.error("Exception on parsing realtime working time {}", appConf.getRealtime().getWorkingTime(), e);
            System.exit(1);
        }
    }

    public BaseHtsConnectionHandler getAConnection() {
        for (RealTimeConnectionHandler connectionHandler : this.connections) {
            if (connectionHandler.connection.isDisconnected() || connectionHandler.connection.checkConnectionDead()) {
                continue;
            }
            return connectionHandler.connection;
        }
        return null;
    }

    public void run() {
        log.warn("do start realtime");
        if (this.isRunning) {
            log.info("already running");
            return;
        }
        this.isRunning = true;
        Calendar calendar = Calendar.getInstance();
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (appConf.getRealtime().getWorkingTime().getWeekDays().stream().noneMatch(day -> day == weekDay)) {
            log.info("it's not in the week day list. Not started");
            this.isRunning = false;
            return;
        }
        calendar.set(1970, Calendar.JANUARY, 1);
        log.info(from.getTimeInMillis() + " - " + to.getTimeInMillis() + " - " + calendar.getTimeInMillis());
        if (from.after(calendar) || to.before(calendar)) {
            log.info("out of working time. Not started");
            this.isRunning = false;
            return;
        }
        log.info("running realtime data listener. start by downloading resource--");
        while (true) {
            try {
                downloadResource().join();
                break;
            } catch (Exception e) {
                log.error("fail to get symbols", e);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        this.start();
    }

    public void stop() {
        log.warn("do stop realtime");
        this.isRunning = false;
        this.clearConnection();
    }

    private void clearConnection() {
        for (RealTimeConnectionHandler con : this.connections) {
            con.stop();
        }
        this.connections.clear();
    }

    private AppConf.RealtimeAccountConf createMultipleAccountConfItem(AppConf.RealtimeAccountConf acc, AppConf.RealtimeAccountConf subItem) {
        List<AppConf.RealtimeAccountConf> current = subItem.getMultipleConnections();
        AppConf.RealtimeAccountConf newAccount = new AppConf.RealtimeAccountConf();
        newAccount.update(acc);
        newAccount.update(subItem);
        // to avoid infinitive loop
        newAccount.setMultipleConnections(current);
        return newAccount;
    }

    private void startAccount(AppConf.RealtimeAccountConf acc, AtomicInteger delay) {
        if (!acc.isMultipleConnectionsSplitBySorted() && acc.getMultipleConnections() != null && !acc.getMultipleConnections().isEmpty()) {
            acc.getMultipleConnections().forEach(a -> {
                AppConf.RealtimeAccountConf newAccount = createMultipleAccountConfItem(acc, a);
                startAccount(newAccount, delay);
            });
            return;
        }
        Set<String> codes = CollectionUtils.isEmpty(acc.getOnlyCodes()) ? getCodes(acc.getCodeType().name(), acc.getExchange()) : acc.getOnlyCodes();
        log.info("starting account {} - {} - {} codes", acc.getName(), acc.getMultipleConnections(), codes.size());
        if (codes.isEmpty()) {
            log.warn("there is no codes for account {}", acc.getName());
            return;
        }
        Map<String, String> addedSymbolMap = new HashMap<>();
        List<String> sortedCodes = new ArrayList<>();
        if (CollectionUtils.isEmpty(monitorService.getOrderSymbols())) {
            sortedCodes.addAll(codes);
        } else {
            monitorService.getOrderSymbols().forEach(symbol -> {
                if (codes.contains(symbol)) {
                    sortedCodes.add(symbol);
                    addedSymbolMap.put(symbol, symbol);
                }
            });

            codes.forEach(symbol -> {
                if (!addedSymbolMap.containsKey(symbol)) {
                    sortedCodes.add(symbol);
                }
            });
        }
        if (acc.isMultipleConnectionsSplitBySorted() && acc.getMultipleConnections() != null && !acc.getMultipleConnections().isEmpty()) {
            LoopLinkedList<Set<String>> codeSets = new LoopLinkedList<>();
            acc.getMultipleConnections().forEach(a -> codeSets.add(new HashSet<>()));
            sortedCodes.forEach(s -> codeSets.next().add(s));
            acc.getMultipleConnections().forEach(a -> {
                AppConf.RealtimeAccountConf newAccount = createMultipleAccountConfItem(acc, a);
                Set<String> onlyCodes = codeSets.next();
                newAccount.setOnlyCodes(onlyCodes);
                log.info("divided codes: {} - {} codes", codeSets, codeSets.size());
                startAccount(newAccount, delay);
            });
            return;
        }
        this.connections.add(new RealTimeConnectionHandler(this, acc, sortedCodes, delay.getAndIncrement() * appConf.getRealtime().getConnectionDelay()));
    }


    private void startWs(AppConf.WebSocketConf con) {
        Set<String> codes = getCodes(con.getCodeType(), con.getExchange());
        log.info("starting ws {} - {} codes", con.getName(), codes.size());
//        Map<String, String> addedSymbolMap = new HashMap<>();
//        List<String> sortedCodes = new ArrayList<>();
//        if (CollectionUtils.isEmpty(monitorService.getOrderSymbols())) {
//            sortedCodes.addAll(codes);
//        } else {
//            monitorService.getOrderSymbols().forEach(symbol -> {
//                if (codes.contains(symbol)) {
//                    sortedCodes.add(symbol);
//                    addedSymbolMap.put(symbol, symbol);
//                }
//            });
//
//            codes.forEach(symbol -> {
//                if (!addedSymbolMap.containsKey(symbol)) {
//                    sortedCodes.add(symbol);
//                }
//            });
//        }
        new Thread(new WsConnectionThread(appConf, con, createRequestSender(), objectMapper, marketRedisDao, cacheService, codes)).start();
    }

    RequestSender createRequestSender() {
        return new RequestSender(objectMapper, appConf);
    }

    private Set<String> getCodes(String codeType, MarketTypeEnum exchange) {
        String ex = exchange == null ? null : exchange.name();
        Set<String> result = new HashSet<>();
        if (this.symbols != null) {
            symbols.forEach(s -> {
                if (codeType != null && (s.getType() == null || !codeType.equals(s.getType().name()))) {
                    return;
                }
                if (ex != null && (s.getMarketType() == null || !s.getMarketType().equals(ex))) {
                    return;
                }
                if (s.getType() == SymbolTypeEnum.INDEX) {
                    result.add(s.getRefCode());
                } else {
                    result.add(s.getCode());
                }
            });
        } else if (securitiesInfoList != null) {
            securitiesInfoList.forEach(s -> {
                if (codeType != null && (s.getType() == null || !codeType.equals(s.getType().name()))) {
                    return;
                }
                if (ex != null && (s.getMarketType() == null || !s.getMarketType().equals(ex))) {
                    return;
                }
                result.add(s.getCode());
            });
        }
        return result;
    }

    private void start() {
        AtomicInteger delay = new AtomicInteger();
        this.clearConnection();
        this.sendOutMap = appConf.getRealtime().getTopics();
        log.info("using sendoutMap '{}'", this.sendOutMap);

        appConf.getRealtime().getAccounts().forEach(i -> this.startAccount(i, delay));
        appConf.getRealtime().getWebsocketConnections().forEach(this::startWs);
    }

    private CompletableFuture<Void> downloadResource() {
        log.info("download resource");
        if (appConf.isEnableUsingRedisForRealtime()) {
            securitiesInfoList = marketRedisDao.getAllSymbolInfo();
            return CompletableFuture.completedFuture(null);
        } else {
            return downloadSymbolListService.downloadFuture(false)
                    .handle((data, err) -> {
                        if (err != null) {
                            securitiesInfoList = marketRedisDao.getAllSymbolInfo();
                        } else {
                            this.symbols = data.values();
                        }
                        return null;
                    });
        }
    }
}
