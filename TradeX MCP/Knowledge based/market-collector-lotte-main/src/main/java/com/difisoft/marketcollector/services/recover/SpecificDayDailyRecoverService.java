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
import com.difisoft.marketcollector.services.HtsConnectionService;
import com.difisoft.model.utils.CompletablePool;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.lock.SingleResourceCreationFutureLock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class SpecificDayDailyRecoverService {
    private final static int COUNT = 20;
    private final MarketRedisDao marketRedisDao;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private final AppConf appConf;

    private SingleResourceCreationFutureLock<BaseHtsConnectionHandler> connectionController;
    private String today;

    private final AtomicBoolean isDownloading = new AtomicBoolean(false);
    private final List<CompletableFuture<Object>> downloadFutures = new ArrayList<>();


    public SpecificDayDailyRecoverService(
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

    public CompletableFuture<Object> downloadFromRequest(String request, RequestContext<Object> ctx) {
        return this.download(request);
    }

    public CompletableFuture<Object> downloadFromRequestNoCache(String request, RequestContext<Object> ctx) {
        return this.download(request);
    }

    public CompletableFuture<Object> download(String date) {
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
        marketRedisDao.getAllSymbolInfo().stream()
                .filter(Objects::nonNull)
//                .filter(it -> it.getType().equals(SymbolTypeEnum.STOCK))
                .forEach(it -> {
                    codes.add(it.getCode());
                    mapSymbolInfo.put(it.getCode(), it);
                });
        downloadDailyAllSymbol(date, mapSymbol.get(), mapSymbolInfo, codes);
        return result;
    }

    private CompletableFuture<Void> downloadDailyAllSymbol(
            String date,
            Map<String, Symbol> mapSymbol,
            Map<String, SymbolInfo> mapSymbolInfo,
            Set<String> codes
    ) {
        ZonedDateTime lowestDate = DefaultUtils.parseZonedDate(date);
        List<SymbolDaily> symbolDailies = new ArrayList<>();
        CompletablePool<DownloadResult> completablePool = new CompletablePool<>(
                codes.stream().map(s -> () -> {
                    Symbol symbol = mapSymbol.get(s);
                    SymbolInfo symbolInfo = mapSymbolInfo.get(s);
                    SymbolTypeEnum type = symbol != null ? symbol.getType() : symbolInfo.getType();
                    CompletableFuture<DownloadResult> future;
                    if (type == SymbolTypeEnum.INDEX) {
                        future = downloadIndexDaily(symbol, symbolInfo, s, lowestDate);
                    } else if (type == SymbolTypeEnum.STOCK) {
                        future = downloadStockDaily(symbol, symbolInfo, s, lowestDate);
                    } else if (type == SymbolTypeEnum.CW) {
                        future = downloadCwDaily(symbol, symbolInfo, s, lowestDate);
                    } else if (type == SymbolTypeEnum.ETF) {
                        future = downloadStockDaily(symbol, symbolInfo, s, lowestDate);
                    } else if (type == SymbolTypeEnum.FUTURES) {
                        future = downloadFuturesDaily(symbol, symbolInfo, s, lowestDate);
                    } else {
                        return CompletableFuture.completedFuture(new DownloadResult());
                    }
                    ZonedDateTime start = lowestDate.truncatedTo(ChronoUnit.DAYS).minusSeconds(1);
                    ZonedDateTime end = start.plusDays(1).plusSeconds(1);

                    return future.handle((r, e) -> {
                        if (e != null) {
                            throw new RuntimeException(e);
                        }
                        r.list.forEach(it -> {
                            ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(it.getDate().getTime()), DefaultUtils.UTC_ID);
                            if (time.isAfter(start) && time.isBefore(end)) {
                                log.info("adding item {}-{}", it.getCode(), it.getDate());
                                symbolDailies.add(it);
                            }
                        });
                        return null;
                    });
                }),
                100
        );
        return completablePool.executeInPoolChain().thenAccept(t -> {
            log.info("finish download all symbols. success {} / total {}", completablePool.getFutureResults().getSuccess().size(), codes.size());
            MongoBulkUtils.updateInBulk(mongoTemplate, 300, symbolDailies, SymbolDaily.class);
            log.info("finish update in bulk {}", symbolDailies.size());
            this.downloadFutures.forEach(it -> it.complete(String.format("finish download all symbols. success %d / total %d", completablePool.getFutureResults().getSuccess().size(), codes.size())));
            isDownloading.set(false);
        });
    }

    private CompletableFuture<DownloadResult> downloadIndexDaily(
            Symbol symbol,
            SymbolInfo symbolInfo,
            String code,
            ZonedDateTime lowestDate
    ) {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        downloadIndexDailyLoop(symbol, symbolInfo, future, new DownloadResult(), code, null, 0, null, lowestDate);
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
            Throwable lastException,
            ZonedDateTime lowestDate
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
        snd.getIReadCount().setValue(COUNT);
        snd.getBaseDate().setValue(baseDate == null ? this.today : DefaultUtils.formatDate(baseDate));
        snd.setStatus(new MarketIndexStatus());
        connectionController.getResource().handle((connection, createConErr) -> {
            if (createConErr != null) {
                downloadIndexDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, createConErr, lowestDate);
            } else {
                connection.sendMessageFuture(snd, MarketIndexDailyRcv.class).handle((rcv, queryErr) -> {
                    if (queryErr != null) {
                        downloadIndexDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, queryErr, lowestDate);
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
                        if (newBaseDate.get() == null || newBaseDate.get().getYear() <= 2012 || newBaseDate.get().isBefore(lowestDate)) {
                            future.complete(result);
                            return null;
                        }
                        downloadIndexDailyLoop(symbol, symbolInfo, future, result, code, newBaseDate.get().minusDays(1), 0, null, lowestDate);
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
            String code,
            ZonedDateTime lowestDate
    ) {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        downloadStockDailyLoop(symbol, symbolInfo, future, new DownloadResult(), code, lowestDate, 0, null, lowestDate);
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
            Throwable lastException,
            ZonedDateTime lowestDate
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
        snd.getIReadCount().setValue(COUNT);
        snd.setStatus(new MarketStockStatus());
        connectionController.getResource().handle((connection, createConErr) -> {
            if (createConErr != null) {
                downloadStockDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, createConErr, lowestDate);
            } else {
                connection.sendMessageFuture(snd, MarketStockDailyRcv.class).handle((rcv, queryErr) -> {
                    log.info("receive results: {}", rcv != null);
                    if (queryErr != null) {
                        downloadStockDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, queryErr, lowestDate);
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
//                            symbolDaily.setReferencePrice((double)it.getChange().getValue());
                            symbolDaily.setRefCode(symbol != null ? symbol.getRefCode() : symbolInfo.getRefCode());
                            symbolDaily.setCreatedAt(symbolDaily.getDate());
                            symbolDaily.setUpdatedAt(symbolDaily.getCreatedAt());
//                            protected Double iNAV;
//                            protected Double iIndexValue;
                            result.add(symbolDaily);
                        });
                        if (newBaseDate.get() == null || newBaseDate.get().isBefore(lowestDate)) {
                            future.complete(result);
                            return null;
                        }
                        ZonedDateTime newDate = newBaseDate.get().minusDays(1L);
                        downloadStockDailyLoop(symbol, symbolInfo, future, result, code, newDate, 0, null, lowestDate);
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
            String code,
            ZonedDateTime lowestDate
    ) {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        downloadCwDailyLoop(symbol, symbolInfo, future, new DownloadResult(), code, lowestDate, 0, null, lowestDate);
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
            Throwable lastException,
            ZonedDateTime lowestDate
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
        snd.getIReadCount().setValue(COUNT);
        snd.getBaseDate().setValue(baseDate == null ? this.today : DefaultUtils.formatDate(baseDate));
        connectionController.getResource().handle((connection, createConErr) -> {
            if (createConErr != null) {
                downloadCwDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, createConErr, lowestDate);
            } else {
                connection.sendMessageFuture(snd, MarketCWDailyRcv.class).handle((rcv, queryErr) -> {
                    if (queryErr != null) {
                        downloadCwDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, queryErr, lowestDate);
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
//                            symbolDaily.setReferencePrice((double)it.getChange().getValue());
                            symbolDaily.setRefCode(symbol != null ? symbol.getRefCode() : symbolInfo.getRefCode());
                            symbolDaily.setCreatedAt(symbolDaily.getDate());
                            symbolDaily.setUpdatedAt(new Date());
//                            protected Double iNAV;
//                            protected Double iIndexValue;
                            result.add(symbolDaily);
                        });
                        if (newBaseDate.get() == null || newBaseDate.get().isBefore(lowestDate)) {
                            future.complete(result);
                            return null;
                        }
                        downloadCwDailyLoop(symbol, symbolInfo, future, result, code, newBaseDate.get().minusDays(1), 0, null, lowestDate);
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
            String code,
            ZonedDateTime lowestDate
    ) {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        downloadFuturesDailyLoop(symbol, symbolInfo, future, new DownloadResult(), code, lowestDate, 0, null, lowestDate);
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
            Throwable lastException,
            ZonedDateTime lowestDate
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
        snd.getIReadCount().setValue(COUNT);
        snd.getBaseDate().setValue(baseDate == null ? this.today : DefaultUtils.formatDate(baseDate));
        connectionController.getResource().handle((connection, createConErr) -> {
            if (createConErr != null) {
                downloadFuturesDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, createConErr, lowestDate);
            } else {
                connection.sendMessageFuture(snd, MarketCWDailyRcv.class).handle((rcv, queryErr) -> {
                    if (queryErr != null) {
                        downloadFuturesDailyLoop(symbol, symbolInfo, future, result, code, baseDate, retry + 1, queryErr, lowestDate);
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
//                            symbolDaily.setReferencePrice((double)it.getChange().getValue());
                            symbolDaily.setRefCode(symbol != null ? symbol.getRefCode() : symbolInfo.getRefCode());
                            symbolDaily.setCreatedAt(symbolDaily.getDate());
                            symbolDaily.setUpdatedAt(new Date());
//                            protected Double iNAV;
//                            protected Double iIndexValue;
                            result.add(symbolDaily);
                        });
                        if (newBaseDate.get() == null || newBaseDate.get().isBefore(lowestDate)) {
                            future.complete(result);
                            return null;
                        }
                        downloadFuturesDailyLoop(symbol, symbolInfo, future, result, code, newBaseDate.get().minusDays(1), 0, null, lowestDate);
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
