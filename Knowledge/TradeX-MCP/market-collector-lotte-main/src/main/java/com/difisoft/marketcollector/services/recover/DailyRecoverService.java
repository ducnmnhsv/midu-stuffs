package com.difisoft.marketcollector.services.recover;

import com.difisoft.htsconnection.socket.message.receive.*;
import com.difisoft.htsconnection.socket.message.send.MarketCWDailySnd;
import com.difisoft.htsconnection.socket.message.send.MarketIndexDailySnd;
import com.difisoft.htsconnection.socket.message.send.MarketStockDailySnd;
import com.difisoft.htsconnection.socket.nonblocking.BaseHtsConnectionHandler;
import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.utils.MongoBulkUtils;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolDaily;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.db.Symbol;
import com.difisoft.marketcollector.services.DownloadSymbolListService;
import com.difisoft.marketcollector.services.HtsConnectionService;
import com.difisoft.model.utils.CompletablePool;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.FutureUtils;
import com.difisoft.model.utils.lock.SingleResourceCreationFutureLock;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class DailyRecoverService {
    private final static String DIR = DailyRecoverService.class.getSimpleName();
    private final static String STATUS_FILE = "status.json";
    private final MarketRedisDao marketRedisDao;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private final AppConf appConf;

    private SingleResourceCreationFutureLock<BaseHtsConnectionHandler> connectionController;
    private String today;

    private final AtomicBoolean isDownloading = new AtomicBoolean(false);
    private final List<CompletableFuture<Object>> downloadFutures = new ArrayList<>();


    public DailyRecoverService(
            MarketRedisDao marketRedisDao,
            HtsConnectionService htsConnectionService,
            ObjectMapper objectMapper,
            MongoTemplate mongoTemplate,
            AppConf appConf
    ) {
        this.marketRedisDao = marketRedisDao;
        this.objectMapper = objectMapper;
        this.mongoTemplate = mongoTemplate;
        this.appConf = appConf;
        connectionController = new SingleResourceCreationFutureLock<>(
                () -> htsConnectionService.createConnection(appConf.getAccountDownload(), 0, (err) -> {
                    log.info("connection is disconnected.");
                    connectionController.invalidateResource();
                }));
    }

    public CompletableFuture<Object> downloadFromRequest(Object request, RequestContext<Object> ctx) {
        return this.download(false);
    }

    public CompletableFuture<Object> downloadFromRequestNoCache(Object request, RequestContext<Object> ctx) {
        return this.download(true);
    }

    public CompletableFuture<Object> download(boolean clearCache) {
        CompletableFuture<Object> result = new CompletableFuture<>();
        if (isDownloading.get()) {
            this.downloadFutures.add(result);
            return result;
        }
        isDownloading.set(true);
        this.downloadFutures.add(result);
        Set<String> codes = new HashSet<>();
        log.info("download symbol list");
        AtomicReference<Map<String, Symbol>> mapSymbol = new AtomicReference<>(new HashMap<>());
        Map<String, SymbolInfo> mapSymbolInfo = new HashMap<>();
        this.today = DefaultUtils.formatDate(ZonedDateTime.now());
        marketRedisDao.getAllSymbolInfo().forEach(it -> {
//            if (it.getType().equals(SymbolTypeEnum.ETF)) {
            codes.add(it.getCode());
            mapSymbolInfo.put(it.getCode(), it);
//            }
        });
        downloadDailyAllSymbol(mapSymbol.get(), mapSymbolInfo, codes, clearCache);
        return result;
    }

    private CompletableFuture<Void> downloadDailyAllSymbol(
            Map<String, Symbol> mapSymbol,
            Map<String, SymbolInfo> mapSymbolInfo,
            Set<String> codes,
            boolean notUsingStatus
    ) {
        ConcurrentHashMap<String, Status> mapStatus = new ConcurrentHashMap<>();
        String statusFile = appConf.getRecover().getDataFolder() + DIR + "/" + STATUS_FILE;
        try {
            log.info("create folder");
            new File(appConf.getRecover().getDataFolder() + DIR).mkdirs();
        } catch (Exception e) {
            log.error("fail to create dir", e);
        }
        if (!notUsingStatus) {
            try {
                mapStatus.putAll(objectMapper.readValue(new File(statusFile), new TypeReference<>() {
                }));
            } catch (IOException e) {
                log.error("fail to read status file", e);
            }
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    objectMapper.writeValue(new File(statusFile), mapStatus);
                } catch (IOException exx) {
                    log.error("fail to write status file", exx);
                }
            }
        }, 3000, 3000);

        CompletablePool<DownloadResult> completablePool = new CompletablePool<>(
                codes.stream().map(s -> () -> {
                    Status status = mapStatus.get(s);
                    String jsonFileName = appConf.getRecover().getDataFolder() + DIR + "/" + s + ".json";
                    if (status != null) {
                        if (status.success) {
                            return CompletableFuture.completedFuture(new DownloadResult());
                        }
                        if (status.hasFile) {
                            try {
                                List<SymbolDaily> list = objectMapper.readValue(new File(jsonFileName), new TypeReference<>() {
                                });
                                try {
                                    return CompletableFuture.completedFuture(handleResult(mapStatus, jsonFileName, statusFile, s, new DownloadResult(list), null));
                                } catch (Exception e) {
                                    return FutureUtils.completeExceptionally(e);
                                }
                            } catch (Exception e) {
                                log.error("fail to read from json data file", e);
                            }
                        }
                    }
                    Symbol symbol = mapSymbol.get(s);
                    SymbolInfo symbolInfo = mapSymbolInfo.get(s);
                    SymbolTypeEnum type = symbol != null ? symbol.getType() : symbolInfo.getType();
                    CompletableFuture<DownloadResult> future;
                    if (type == SymbolTypeEnum.INDEX) {
                        future = downloadIndexDaily(symbol, symbolInfo, s);
                    } else if (type == SymbolTypeEnum.STOCK) {
                        future = downloadStockDaily(symbol, symbolInfo, s);
                    } else if (type == SymbolTypeEnum.CW) {
                        future = downloadCwDaily(symbol, symbolInfo, s);
                    } else if (type == SymbolTypeEnum.ETF) {
                        future = downloadStockDaily(symbol, symbolInfo, s);
                    } else if (type == SymbolTypeEnum.FUTURES) {
                        future = downloadFuturesDaily(symbol, symbolInfo, s);
                    } else {
                        return CompletableFuture.completedFuture(new DownloadResult());
                    }

                    return future.handle((r, e) -> handleResult(mapStatus, jsonFileName, statusFile, s, r, e));
                }),
                100
        );
        return completablePool.executeInPoolChain().thenAccept(t -> {
            try {
                objectMapper.writeValue(new File(statusFile), mapStatus);
            } catch (IOException ex) {
                log.error("fail to write status file", ex);
            }
            timer.cancel();
            log.info("finish download all symbols. success {} / total {}", completablePool.getFutureResults().getSuccess().size(), codes.size());
            this.downloadFutures.forEach(it -> it.complete(String.format("finish download all symbols. success %d / total %d", completablePool.getFutureResults().getSuccess().size(), codes.size())));
            isDownloading.set(false);
        });
    }

    private DownloadResult handleResult(Map<String, Status> mapStatus, String jsonFileName, String statusFile, String code, DownloadResult r, Throwable e) {
        if (e != null) {
            mapStatus.put(code, new Status(code, false, e.getMessage()));
            throw new CompletionException(e);
        } else {
            boolean hasFile = false;
            try {
                objectMapper.writeValue(new File(jsonFileName), r.list);
                hasFile = true;
            } catch (IOException ex) {
                log.error("fail to write data to file", ex);
            }
            try {
                removeAndInsertMongo(code, r.list);
                mapStatus.put(code, new Status(code));
            } catch (RuntimeException ex) {
                mapStatus.put(code, new Status(code, hasFile, ex.getMessage()));
                throw ex;
            }
        }
        return r;
    }

    private void removeAndInsertMongo(String code, List<SymbolDaily> list) {
        Query removeQuery = new Query(Criteria.where("code").is(code));
        DeleteResult deleteResult = mongoTemplate.remove(removeQuery, SymbolDaily.class);
        log.info("remove {} items of {} in symbol daily", deleteResult.getDeletedCount(), code);
        MongoBulkUtils.insertInBulk(mongoTemplate, 300, list, SymbolDaily.class);
        log.info("inserted {} items of {} in symbol daily", list.size(), code);
    }

    private CompletableFuture<DownloadResult> downloadIndexDaily(
            Symbol symbol,
            SymbolInfo symbolInfo,
            String code
    ) {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        downloadIndexDailyLoop(symbol, symbolInfo, future, new DownloadResult(), code, null, 0, null);
        return future;
    }

    private void downloadIndexDailyLoop(
            Symbol symbol,
            SymbolInfo symbolInfo,
            CompletableFuture<DownloadResult> future,
            DownloadResult result,
            String code,
            ZonedDateTime baseDate,
            int retry,
            Throwable lastException
    ) {
        if (retry > 0) {
            log.error("fail to download symbol {} {} {}", code, baseDate, result.list.size(), lastException);
        }
        if (retry > 5) {
            result.lastException = lastException;
            future.complete(result);
            return;
        }
        log.info("download daily of {} baseDate {} retry: {}. total: {}", code, baseDate, retry, result.list.size());
        MarketIndexDailySnd snd = new MarketIndexDailySnd();
        snd.getIndexCode().setValue(symbol != null ? symbol.getRefCode() : symbolInfo.getRefCode());
        snd.getIReadCount().setValue(100);
        snd.getBaseDate().setValue(baseDate == null ? this.today : DefaultUtils.formatDate(baseDate));
        snd.setStatus(new MarketIndexStatus());
        connectionController.getResource().handle((connection, createConErr) -> {
            if (createConErr != null) {
                downloadIndexDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, createConErr);
            } else {
                connection.sendMessageFuture(snd, MarketIndexDailyRcv.class).handle((rcv, queryErr) -> {
                    if (queryErr != null) {
                        downloadIndexDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, queryErr);
                    } else {
                        AtomicReference<ZonedDateTime> newBaseDate = new AtomicReference<>(null);
                        rcv.getItems().forEach(it -> {
                            SymbolDaily symbolDaily = new SymbolDaily();
                            newBaseDate.set(DefaultUtils.parseZonedDate(it.getDate().getValue()));
                            symbolDaily.setId(String.format("%s_%s", code, it.getDate().getValue()));
                            symbolDaily.setCode(code);
                            symbolDaily.setDate(Date.from(newBaseDate.get().toInstant()));
                            symbolDaily.setType(symbol != null ? symbol.getType() : symbolInfo.getType());
                            symbolDaily.setMarketType(symbol != null ? symbol.getMarketType() : symbolInfo.getMarketType());
                            symbolDaily.setChange((double) it.getChange().getValue());
                            symbolDaily.setRate((double) it.getRate().getValue());
                            symbolDaily.setTradingVolume(it.getTradingVolume().getValue());
                            symbolDaily.setTradingValue((double) it.getTradingValue().getValue() * 1000000);
                            symbolDaily.setOpen((double) it.getOpen().getValue());
                            symbolDaily.setHigh((double) it.getHigh().getValue());
                            symbolDaily.setLow((double) it.getLow().getValue());
                            symbolDaily.setLast((double) it.getLast().getValue());
                            symbolDaily.setRefCode(symbol != null ? symbol.getRefCode() : symbolInfo.getRefCode());
                            symbolDaily.setCreatedAt(symbolDaily.getDate());
                            symbolDaily.setUpdatedAt(new Date());
                            result.add(symbolDaily);
                        });
                        if (newBaseDate.get() == null) {
                            future.complete(result);
                            return null;
                        }
                        ZonedDateTime newDate = newBaseDate.get().minusDays(1);
                        if (newDate.getYear() <= 2012) {
                            future.complete(result);
                            return null;
                        }
                        downloadIndexDailyLoop(symbol, symbolInfo, future, result, code, newDate, 0, null);
                    }
                    return null;
                });
            }
            return null;
        });
    }

    private CompletableFuture<DownloadResult> downloadStockDaily(
            Symbol symbol,
            SymbolInfo symbolInfo,
            String code
    ) {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        downloadStockDailyLoop(symbol, symbolInfo, future, new DownloadResult(), code, null, 0, null);
        return future;
    }

    private void downloadStockDailyLoop(
            Symbol symbol,
            SymbolInfo symbolInfo,
            CompletableFuture<DownloadResult> future,
            DownloadResult result,
            String code,
            ZonedDateTime baseDate,
            int retry,
            Throwable lastException
    ) {
        if (retry > 0) {
            log.error("fail to download symbol {} {} {}", code, baseDate, result.list.size(), lastException);
        }
        if (retry > 5) {
            result.lastException = lastException;
            future.complete(result);
            return;
        }
        log.info("download daily of {} baseDate {} retry: {}. total: {}", code, baseDate, retry, result.list.size());
        MarketStockDailySnd snd = new MarketStockDailySnd();
        snd.getStockCode().setValue(code);
        snd.getPriceType().setValue(1); // 1 is adjusted, 0 is raw
        snd.getBaseDate().setValue(baseDate == null ? this.today : DefaultUtils.formatDate(baseDate));
        snd.getIReadCount().setValue(100);
        snd.setStatus(new MarketStockStatus());
        connectionController.getResource().handle((connection, createConErr) -> {
            if (createConErr != null) {
                downloadStockDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, createConErr);
            } else {
                connection.sendMessageFuture(snd, MarketStockDailyRcv.class).handle((rcv, queryErr) -> {
                    log.info("receive results: {}", rcv != null);
                    if (queryErr != null) {
                        downloadStockDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, queryErr);
                    } else {
                        AtomicReference<ZonedDateTime> newBaseDate = new AtomicReference<>(null);
                        rcv.getItems().forEach(it -> {
                            SymbolDaily symbolDaily = new SymbolDaily();
                            symbolDaily.setId(String.format("%s_%s", code, it.getDate().getValue()));
                            newBaseDate.set(DefaultUtils.parseZonedDate(it.getDate().getValue()));
                            symbolDaily.setCode(code);
                            symbolDaily.setDate(Date.from(newBaseDate.get().toInstant()));
                            symbolDaily.setType(symbol != null ? symbol.getType() : symbolInfo.getType());
                            symbolDaily.setMarketType(symbol != null ? symbol.getMarketType() : symbolInfo.getMarketType());
                            symbolDaily.setChange((double) it.getChange().getValue());
                            symbolDaily.setRate((double) it.getRate().getValue());
                            symbolDaily.setTradingVolume(it.getTradingVolume().getValue());
                            symbolDaily.setTradingValue((double) it.getTradingValue().getValue() * 1000000);
                            symbolDaily.setOpen((double) it.getOpen().getValue());
                            symbolDaily.setHigh((double) it.getHigh().getValue());
                            symbolDaily.setLow((double) it.getLow().getValue());
                            symbolDaily.setLast((double) it.getLast().getValue());
                            symbolDaily.setRefCode(symbol != null ? symbol.getRefCode() : symbolInfo.getRefCode());
                            symbolDaily.setCreatedAt(symbolDaily.getDate());
                            symbolDaily.setUpdatedAt(symbolDaily.getCreatedAt());
                            result.add(symbolDaily);
                        });
                        if (newBaseDate.get() == null) {
                            future.complete(result);
                            return null;
                        }
                        ZonedDateTime newDate = newBaseDate.get().minusDays(1L);
                        downloadStockDailyLoop(symbol, symbolInfo, future, result, code, newDate, 0, null);
                    }
                    return null;
                });
            }
            return null;
        });
    }

    private CompletableFuture<DownloadResult> downloadCwDaily(
            Symbol symbol,
            SymbolInfo symbolInfo,
            String code
    ) {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        downloadCwDailyLoop(symbol, symbolInfo, future, new DownloadResult(), code, null, 0, null);
        return future;
    }

    private void downloadCwDailyLoop(
            Symbol symbol,
            SymbolInfo symbolInfo,
            CompletableFuture<DownloadResult> future,
            DownloadResult result,
            String code,
            ZonedDateTime baseDate,
            int retry,
            Throwable lastException
    ) {
        if (retry > 0) {
            log.error("fail to download symbol {} {} {}", code, baseDate, result.list.size(), lastException);
        }
        if (retry > 5) {
            result.lastException = lastException;
            future.complete(result);
            return;
        }
        log.info("download daily of {} baseDate {} retry: {}. total: {}", code, baseDate, retry, result.list.size());
        MarketCWDailySnd snd = new MarketCWDailySnd();
        snd.getCwCode().setValue(code);
        snd.getIReadCount().setValue(100);
        snd.getBaseDate().setValue(baseDate == null ? this.today : DefaultUtils.formatDate(baseDate));
        connectionController.getResource().handle((connection, createConErr) -> {
            if (createConErr != null) {
                downloadCwDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, createConErr);
            } else {
                connection.sendMessageFuture(snd, MarketCWDailyRcv.class).handle((rcv, queryErr) -> {
                    if (queryErr != null) {
                        downloadCwDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, queryErr);
                    } else {
                        AtomicReference<ZonedDateTime> newBaseDate = new AtomicReference<>(null);
                        rcv.getItems().forEach(it -> {
                            SymbolDaily symbolDaily = new SymbolDaily();
                            symbolDaily.setId(String.format("%s_%s", code, it.getDate().getValue()));
                            newBaseDate.set(DefaultUtils.parseZonedDate(it.getDate().getValue()));
                            symbolDaily.setCode(code);
                            symbolDaily.setDate(Date.from(newBaseDate.get().toInstant()));
                            symbolDaily.setType(symbol != null ? symbol.getType() : symbolInfo.getType());
                            symbolDaily.setMarketType(symbol != null ? symbol.getMarketType() : symbolInfo.getMarketType());
                            symbolDaily.setChange((double) it.getChange().getValue());
                            symbolDaily.setRate((double) it.getRate().getValue());
                            symbolDaily.setTradingVolume(it.getTradingVolume().getValue());
                            symbolDaily.setTradingValue((double) it.getTradingValue().getValue() * 1000000);
                            symbolDaily.setOpen((double) it.getOpen().getValue());
                            symbolDaily.setHigh((double) it.getHigh().getValue());
                            symbolDaily.setLow((double) it.getLow().getValue());
                            symbolDaily.setLast((double) it.getLast().getValue());
                            symbolDaily.setRefCode(symbol != null ? symbol.getRefCode() : symbolInfo.getRefCode());
                            symbolDaily.setCreatedAt(symbolDaily.getDate());
                            symbolDaily.setUpdatedAt(new Date());
                            result.add(symbolDaily);
                        });
                        if (newBaseDate.get() == null) {
                            future.complete(result);
                            return null;
                        }
                        downloadCwDailyLoop(symbol, symbolInfo, future, result, code, newBaseDate.get().minusDays(1), 0, null);
                    }
                    return null;
                });
            }
            return null;
        });
    }

    private CompletableFuture<DownloadResult> downloadFuturesDaily(
            Symbol symbol,
            SymbolInfo symbolInfo,
            String code
    ) {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        downloadFuturesDailyLoop(symbol, symbolInfo, future, new DownloadResult(), code, null, 0, null);
        return future;
    }

    private void downloadFuturesDailyLoop(
            Symbol symbol,
            SymbolInfo symbolInfo,
            CompletableFuture<DownloadResult> future,
            DownloadResult result,
            String code,
            ZonedDateTime baseDate,
            int retry,
            Throwable lastException
    ) {
        if (retry > 0) {
            log.error("fail to download symbol {} {} {}", code, baseDate, result.list.size(), lastException);
        }
        if (retry > 5) {
            result.lastException = lastException;
            future.complete(result);
            return;
        }
        log.info("download daily of {} baseDate {} retry: {}. total: {}", code, baseDate, retry, result.list.size());
        MarketCWDailySnd snd = new MarketCWDailySnd();
        snd.getCwCode().setValue(code);
        snd.getIReadCount().setValue(100);
        snd.getBaseDate().setValue(baseDate == null ? this.today : DefaultUtils.formatDate(baseDate));
        connectionController.getResource().handle((connection, createConErr) -> {
            if (createConErr != null) {
                downloadFuturesDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, createConErr);
            } else {
                connection.sendMessageFuture(snd, MarketCWDailyRcv.class).handle((rcv, queryErr) -> {
                    if (queryErr != null) {
                        downloadFuturesDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, queryErr);
                    } else {
                        AtomicReference<ZonedDateTime> newBaseDate = new AtomicReference<>(null);
                        rcv.getItems().forEach(it -> {
                            SymbolDaily symbolDaily = new SymbolDaily();
                            symbolDaily.setId(String.format("%s_%s", code, it.getDate().getValue()));
                            newBaseDate.set(DefaultUtils.parseZonedDate(it.getDate().getValue()));
                            symbolDaily.setCode(code);
                            symbolDaily.setDate(Date.from(newBaseDate.get().toInstant()));
                            symbolDaily.setType(symbol != null ? symbol.getType() : symbolInfo.getType());
                            symbolDaily.setMarketType(symbol != null ? symbol.getMarketType() : symbolInfo.getMarketType());
                            symbolDaily.setChange((double) it.getChange().getValue());
                            symbolDaily.setRate((double) it.getRate().getValue());
                            symbolDaily.setTradingVolume(it.getTradingVolume().getValue());
                            symbolDaily.setTradingValue((double) it.getTradingValue().getValue() * 1000000);
                            symbolDaily.setOpen((double) it.getOpen().getValue());
                            symbolDaily.setHigh((double) it.getHigh().getValue());
                            symbolDaily.setLow((double) it.getLow().getValue());
                            symbolDaily.setLast((double) it.getLast().getValue());
                            symbolDaily.setRefCode(symbol != null ? symbol.getRefCode() : symbolInfo.getRefCode());
                            symbolDaily.setCreatedAt(symbolDaily.getDate());
                            symbolDaily.setUpdatedAt(new Date());
                            result.add(symbolDaily);
                        });
                        if (newBaseDate.get() == null) {
                            future.complete(result);
                            return null;
                        }
                        downloadFuturesDailyLoop(symbol, symbolInfo, future, result, code, newBaseDate.get().minusDays(1), 0, null);
                    }
                    return null;
                });
            }
            return null;
        });
    }

    private static class DownloadResult {
        List<SymbolDaily> list = new ArrayList<>();
        Map<String, SymbolDaily> mapDuplicateCheck = new HashMap<>();
        Throwable lastException;

        void add(SymbolDaily s) {
            if (!mapDuplicateCheck.containsKey(s.getId())) {
                list.add(s);
                mapDuplicateCheck.put(s.getId(), s);
            }
        }

        public DownloadResult() {
        }

        public DownloadResult(List<SymbolDaily> list) {
            this.list = list;
        }
    }
}
