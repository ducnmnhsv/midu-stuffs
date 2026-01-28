package com.difisoft.marketcollector.services.recover;

import com.difisoft.htsconnection.socket.message.receive.MarketIndexQuoteListRcv;
import com.difisoft.htsconnection.socket.message.receive.MarketStockQuoteListRcv;
import com.difisoft.htsconnection.socket.message.receive.MarketStockQuoteStatus;
import com.difisoft.htsconnection.socket.message.send.MarketIndexQuoteListSnd;
import com.difisoft.htsconnection.socket.message.send.MarketStockQuoteListSnd;
import com.difisoft.htsconnection.socket.nonblocking.BaseHtsConnectionHandler;
import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.market.model.v2.db.SymbolQuote;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.services.HtsConnectionService;
import com.difisoft.marketcollector.services.RequestSender;
import com.difisoft.model.utils.CompletablePool;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.FutureUtils;
import com.difisoft.model.utils.lock.SingleResourceCreationFutureLock;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@Service
@Slf4j
public class QuoteRecoverService {
    private final static String DIR = QuoteRecoverService.class.getSimpleName();
    private final static String STATUS_FILE = "status.json";
    private final MarketRedisDao marketRedisDao;
    private final ObjectMapper objectMapper;
    private final RequestSender requestSender;
    private final AppConf appConf;

    private SingleResourceCreationFutureLock<BaseHtsConnectionHandler> connectionController;


    public QuoteRecoverService(
            MarketRedisDao marketRedisDao,
            HtsConnectionService htsConnectionService,
            ObjectMapper objectMapper,
            RequestSender requestSender,
            AppConf appConf
    ) {
        this.marketRedisDao = marketRedisDao;
        this.objectMapper = objectMapper;
        this.requestSender = requestSender;
        this.appConf = appConf;
        connectionController = new SingleResourceCreationFutureLock<>(
                () -> htsConnectionService.createConnection(appConf.getAccountDownload(), 0, (err) -> {
                    log.info("connection is disconnected.");
                    connectionController.invalidateResource();
                }));
    }


    public CompletableFuture<Object> downloadFromRequest(String code, RequestContext<String> ctx) {
        this.download(false, Collections.singletonList(code));
        return CompletableFuture.completedFuture("");
    }

    public void download(boolean usingCache, List<String> symbols) {
        log.info("download symbol list");
        Stream<SymbolInfo> stream = marketRedisDao.getAllSymbolInfo().stream()
                .filter(s -> symbols.contains(s.getCode()));
        downloadDailyAllSymbol(stream, usingCache);
    }

    private CompletableFuture<Void> downloadDailyAllSymbol(
            Stream<SymbolInfo> symbols,
            boolean usingCache
    ) {
        Map<String, Status> mapStatus = new HashMap<>();
        String statusFile = appConf.getRecover().getDataFolder() + DIR + "/" + STATUS_FILE;
        try {
            log.info("create folder");
            new File(appConf.getRecover().getDataFolder() + DIR).mkdirs();
        } catch (Exception e) {
            log.error("fail to create dir", e);
        }
        // TODO read status
        CompletablePool<DownloadResult> completablePool = new CompletablePool<>(
                symbols.map(symbolInfo -> () -> {
                    String s = symbolInfo.getCode();
                    Status status = mapStatus.get(s);
                    String jsonFileName = appConf.getRecover().getDataFolder() + DIR + "/" + s + ".json";
                    if (status != null) {
                        if (status.success) {
                            return CompletableFuture.completedFuture(new DownloadResult());
                        }
                        if (status.hasFile) {
                            try {
                                List<SymbolQuote> list = objectMapper.readValue(new File(jsonFileName), new TypeReference<>() {
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
                    SymbolTypeEnum type = symbolInfo.getType();
                    CompletableFuture<DownloadResult> future;
                    if (type == SymbolTypeEnum.INDEX) {
//                        future = downloadIndexQuoteList(symbolInfo, s);
                        return CompletableFuture.completedFuture(new DownloadResult());
                    } else if (type == SymbolTypeEnum.STOCK) {
                        future = downloadStockQuoteLoop(symbolInfo, s);
                    } else if (type == SymbolTypeEnum.CW) {
//                        future = downloadIndexQuoteList(symbolInfo, s);
                        return CompletableFuture.completedFuture(new DownloadResult());
                    } else if (type == SymbolTypeEnum.ETF) {
                        future = downloadStockQuoteLoop(symbolInfo, s);
                    } else if (type == SymbolTypeEnum.FUTURES) {
//                        future = downloadIndexQuoteList(symbolInfo, s);
                        return CompletableFuture.completedFuture(new DownloadResult());
                    } else {
                        return CompletableFuture.completedFuture(new DownloadResult());
                    }

                    return future.handle((r, e) -> handleResult(mapStatus, jsonFileName, statusFile, s, r, e));
                }),
                70
        );
        return completablePool.executeInPoolChain().thenAccept(t -> {
            log.info("finish download all symbols. success {} / total {}", completablePool.getFutureResults().getSuccess().size(), completablePool.getResults().size());
        });
    }

    private DownloadResult handleResult(Map<String, Status> mapStatus, String jsonFileName, String statusFile, String code, DownloadResult r, Throwable e) {
        log.info("download {} - {}", code, r.list.size(), e);
        if (e != null) {
            mapStatus.put(code, new Status(code, false, e.getMessage()));
            throw new CompletionException(e);
        } else {
            r.list.forEach(it -> requestSender.sendMiniMessageSafeNoResponse(appConf.getTopics().getQuoteRecover(), "Update", it));
            boolean hasFile = false;
            try {
                objectMapper.writeValue(new File(jsonFileName), r.list);
                hasFile = true;
            } catch (IOException ex) {
                log.error("fail to write data to file", ex);
            }
            try {
                mapStatus.put(code, new Status(code));
            } catch (RuntimeException ex) {
                mapStatus.put(code, new Status(code, hasFile, ex.getMessage()));
                throw ex;
            }
        }
        try {
            objectMapper.writeValue(new File(statusFile), mapStatus);
        } catch (IOException ex) {
            log.error("fail to write status file", ex);
        }
        return r;
    }

    private CompletableFuture<DownloadResult> downloadIndexQuoteList(
            SymbolInfo symbolInfo,
            String code
    ) {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        downloadIndexQuoteLoop(symbolInfo, future, new DownloadResult(), code, null, 0, null);
        return future;
    }

    private void downloadIndexQuoteLoop(
            SymbolInfo symbolInfo,
            CompletableFuture<DownloadResult> future,
            DownloadResult result,
            String code,
            String baseTime,
            int retry,
            Throwable lastException
    ) {
        if (retry > 0) {
            log.error("fail to download symbol {} {} {}", code, baseTime, result.list.size(), lastException);
        }
        if (retry > 5) {
            result.lastException = lastException;
            future.complete(result);
            return;
        }
        log.info("download daily of {} baseDate {} retry: {}. total: {}", code, baseTime, retry, result.list.size());
        MarketIndexQuoteListSnd marketIndexDailySnd = new MarketIndexQuoteListSnd();
        marketIndexDailySnd.getIndexCode().setValue(symbolInfo.getRefCode());
        marketIndexDailySnd.getIReadCount().setValue(100);
//        marketIndexDailySnd.setStatus(new MarketIndexStatus());
        connectionController.getResource().handle((connection, createConErr) -> {
            if (createConErr != null) {
                downloadIndexQuoteLoop(symbolInfo, future, result, code, baseTime, retry + 1, createConErr);
            } else {
                connection.sendMessageFuture(marketIndexDailySnd, MarketIndexQuoteListRcv.class).handle((rcv, queryErr) -> {
                    if (queryErr != null) {
                        downloadIndexQuoteLoop(symbolInfo, future, result, code, baseTime, retry + 1, queryErr);
                    } else {
                        AtomicReference<ZonedDateTime> newBaseDate = new AtomicReference<>(null);
                        rcv.getItems().forEach(it -> {
                            SymbolQuote quote = new SymbolQuote();
                            quote.setId(String.format("%s_%s", code, newBaseDate.get()));
                            quote.setCode(code);
                            quote.setDate(Date.from(newBaseDate.get().toInstant()));
                            quote.setType(symbolInfo.getType());
                            quote.setChange((double) it.getChange().getValue());
                            quote.setRate((double) it.getRate().getValue());
                            quote.setTradingVolume(it.getTradingVolume().getValue());
                            quote.setTradingValue((double) it.getTradingValue().getValue());
                            quote.setOpen((double) it.getOpen().getValue());
                            quote.setHigh((double) it.getHigh().getValue());
                            quote.setLow((double) it.getLow().getValue());
                            quote.setLast((double) it.getLast().getValue());
                            quote.setRefCode(symbolInfo.getRefCode());
                            quote.setCreatedAt(quote.getDate());
                            quote.setUpdatedAt(new Date());
                            result.add(quote);
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
                        downloadIndexQuoteLoop(symbolInfo, future, result, code, baseTime, 0, null);
                    }
                    return null;
                });
            }
            return null;
        });
    }




    private CompletableFuture<DownloadResult> downloadStockQuoteLoop(
            SymbolInfo symbolInfo,
            String code
    ) {
        CompletableFuture<DownloadResult> future = new CompletableFuture<>();
        downloadStockQuoteLoop(symbolInfo, future, new DownloadResult(), code, null, 0, null);
        return future;
    }

    private void downloadStockQuoteLoop(
            SymbolInfo symbolInfo,
            CompletableFuture<DownloadResult> future,
            DownloadResult result,
            String code,
            MarketStockQuoteStatus marketStockQuoteStatus,
            int retry,
            Throwable lastException
    ) {
        if (retry > 0) {
            log.error("fail to download symbol {} {} {}", code, marketStockQuoteStatus, result.list.size(), lastException);
        }
        if (retry > 5) {
            result.lastException = lastException;
            log.error("fail to finish download {}", symbolInfo.getCode(), lastException);
            future.complete(result);
            return;
        }
        log.info("download daily of {} baseDate {} retry: {}. total: {}", code, marketStockQuoteStatus, retry, result.list.size());
        MarketStockQuoteListSnd snd = new MarketStockQuoteListSnd();
        snd.getStockCode().setValue(symbolInfo.getCode());
        snd.getIReadCount().setValue(20);
//        if (marketStockQuoteStatus == null) {
//            marketStockQuoteStatus = new MarketStockQuoteStatus();
//            marketStockQuoteStatus.getBaseTime().setValue(0);
//            marketStockQuoteStatus.getExt().setValue(0);
//        }
        snd.setStatus(marketStockQuoteStatus);
        MarketStockQuoteStatus finalMarketStockQuoteStatus = marketStockQuoteStatus;
        connectionController.getResource().handle((connection, createConErr) -> {
            if (createConErr != null) {
                downloadStockQuoteLoop(symbolInfo, future, result, code, finalMarketStockQuoteStatus, retry + 1, createConErr);
            } else {
                connection.sendMessageFuture(snd, MarketStockQuoteListRcv.class).handle((rcv, queryErr) -> {
                    if (queryErr != null) {
                        downloadStockQuoteLoop(symbolInfo, future, result, code, finalMarketStockQuoteStatus, retry + 1, queryErr);
                    } else {
                        rcv.getItems().forEach(it -> {
                            SymbolQuote quote = new SymbolQuote();
                            quote.setId(String.format("%s_%d", code, it.getTradingVolume().getValue()));
                            quote.setCode(code);
                            String time = it.getTime().getValue().replace(":", "");
                            ZonedDateTime utcZonedTime = DefaultUtils.parseZonedTimeAtZone(time, DefaultUtils.VIETNAM_ID).withZoneSameInstant(DefaultUtils.UTC_ID);
                            quote.setTime(DefaultUtils.formatTime(utcZonedTime));
                            quote.setDate(new Date());
                            quote.setType(symbolInfo.getType());
                            quote.setChange((double) it.getChange().getValue());
                            quote.setRate((double) it.getRate().getValue());
                            quote.setMatchingVolume((long) it.getMatchVolume().getValue());
                            quote.setTradingVolume(it.getTradingVolume().getValue());
                            quote.setOpen((double) it.getOpen().getValue());
                            quote.setHigh((double) it.getHigh().getValue());
                            quote.setLow((double) it.getLow().getValue());
                            quote.setLast((double) it.getLast().getValue());
                            quote.setRefCode(symbolInfo.getRefCode());
                            quote.setCreatedAt(quote.getDate());
                            quote.setUpdatedAt(new Date());
                            result.add(quote);
                        });
                        if (rcv.getItems().size() < 20) {
                            future.complete(result);
                            return null;
                        }
                        if (rcv.getStatus() == null || (
                                finalMarketStockQuoteStatus != null && finalMarketStockQuoteStatus.getBaseTime().getValue() > 0 &&
                                        rcv.getStatus().getBaseTime().getValue() >= finalMarketStockQuoteStatus.getBaseTime().getValue()
                        )) {
                            future.complete(result);
                            return null;
                        }
                        downloadStockQuoteLoop(symbolInfo, future, result, code, rcv.getStatus(), 0, null);
                    }
                    return null;
                });
            }
            return null;
        });
    }




    private static class DownloadResult {
        List<SymbolQuote> list = new ArrayList<>();
        Map<Long, SymbolQuote> mapDuplicateCheck = new HashMap<>();
        Throwable lastException;

        void add(SymbolQuote s) {
            if (!mapDuplicateCheck.containsKey(s.getTradingVolume())) {
                list.add(s);
                mapDuplicateCheck.put(s.getTradingVolume(), s);
            }
        }

        public DownloadResult() {
        }

        public DownloadResult(List<SymbolQuote> list) {
            this.list = list;
        }
    }

    @Getter
    public static class Status {
        String code;
        boolean hasFile;
        boolean success;
        String failMessage;

        public Status(String code) {
            this.code = code;
            this.success = true;
            this.hasFile = true;
        }

        public Status(String code, boolean hasFile, String failMessage) {
            this.code = code;
            this.hasFile = hasFile;
            this.success = false;
            this.failMessage = failMessage;
        }
    }
}
