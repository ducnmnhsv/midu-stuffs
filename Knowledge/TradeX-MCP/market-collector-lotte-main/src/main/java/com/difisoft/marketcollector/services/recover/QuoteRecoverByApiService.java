package com.difisoft.marketcollector.services.recover;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.market.model.v2.db.SymbolQuote;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.lotte.api.CwQuoteResponse;
import com.difisoft.marketcollector.model.lotte.api.EtfQuoteResponse;
import com.difisoft.marketcollector.model.lotte.api.IndexQuoteResponse;
import com.difisoft.marketcollector.model.lotte.api.StockQuoteResponse;
import com.difisoft.marketcollector.services.LotteApiService;
import com.difisoft.marketcollector.services.RequestSender;
import com.difisoft.model.utils.MultiThreadCompletablePool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class QuoteRecoverByApiService {
    private final static String DIR = QuoteRecoverByApiService.class.getSimpleName();
    private final static String STATUS_FILE = "status.json";
    private static final String logId = QuoteRecoverByApiService.class.getSimpleName();

    private final AppConf appConf;
    private final MarketRedisDao marketRedisDao;
    private final LotteApiService lotteApiService;
    private final ObjectMapper objectMapper;
    private final RequestSender requestSender;


    public QuoteRecoverByApiService(
            AppConf appConf,
            MarketRedisDao marketRedisDao,
            LotteApiService lotteApiService,
            ObjectMapper objectMapper,
            RequestSender requestSender
    ) {
        this.appConf = appConf;
        this.marketRedisDao = marketRedisDao;
        this.lotteApiService = lotteApiService;
        this.objectMapper = objectMapper;
        this.requestSender = requestSender;
    }

    public void download(boolean clearCache) {
        ConcurrentHashMap<String, Status> mapStatus = new ConcurrentHashMap<>();
        String statusFile = appConf.getRecover().getDataFolder() + DIR + "/" + STATUS_FILE;
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
        try {
            log.info("create folder");
            new File(appConf.getRecover().getDataFolder() + DIR).mkdirs();
        } catch (Exception e) {
            log.error("fail to create dir", e);
        }
        if (!clearCache) {
            try {
                mapStatus.putAll(objectMapper.readValue(new File(statusFile), new TypeReference<>() {
                }));
            } catch (IOException e) {
                log.error("fail to read status file", e);
            }
        }

        Stream<SymbolInfo> symbolInfoList = marketRedisDao.getAllSymbolInfo()
                .stream()
                .filter(it -> it.getCode().equals("HNX"));
        MultiThreadCompletablePool<String> pool = new MultiThreadCompletablePool<>(
                symbolInfoList.map(s -> (Supplier<String>) () -> queryQuotes(mapStatus, s)).collect(Collectors.toList()),
                50
        );
        pool.execute().thenAccept(r -> {
            try {
                objectMapper.writeValue(new File(statusFile), mapStatus);
            } catch (IOException exx) {
                log.error("fail to write status file", exx);
            }
            timer.cancel();
            log.error("finish update daily. success {} / total {}", pool.getSuccessResults().size(), pool.getResults().size());
        });
    }

    private String queryQuotes(Map<String, Status> mapStatus, SymbolInfo symbolInfo) {
        List<SymbolQuote> quotes = null;

        Status status = mapStatus.get(symbolInfo.getCode());
        String jsonFileName = appConf.getRecover().getDataFolder() + DIR + "/" + symbolInfo.getCode() + ".json";
        if (status != null) {
            if (status.success) {
                return null;
            }
            if (status.hasFile) {
                try {
                    quotes = objectMapper.readValue(new File(jsonFileName), new TypeReference<>() {
                    });
                } catch (Exception e) {
                    log.error("fail to read from json data file", e);
                }
            }
        }

        boolean hasFile = quotes != null;
        if (quotes == null) {
            try {
                if (symbolInfo.getType() == SymbolTypeEnum.INDEX) {
                    quotes = queryIndexQuotes(symbolInfo);
                } else if (symbolInfo.getType() == SymbolTypeEnum.STOCK) {
                    quotes = queryStockQuotes(symbolInfo);
                } else if (symbolInfo.getType() == SymbolTypeEnum.CW) {
                    quotes = queryCwQuotes(symbolInfo);
                } else if (symbolInfo.getType() == SymbolTypeEnum.ETF) {
                    quotes = queryEtfQuotes(symbolInfo);
                } else {
                    return null;
                }
                quotes.forEach(it -> {
                    requestSender.sendMiniMessageSafeNoResponse(appConf.getTopics().getQuoteRecover(), "Update", it);
                });
            } catch (RuntimeException ex) {
                log.error("fail to write data to file", ex);
                mapStatus.put(symbolInfo.getCode(), new Status(symbolInfo.getCode(), hasFile, ex.getMessage()));
                throw ex;
            }
            try {
                objectMapper.writeValue(new File(jsonFileName), quotes);
                mapStatus.put(symbolInfo.getCode(), new Status(symbolInfo.getCode()));
            } catch (IOException ex) {
                log.error("fail to write data to file", ex);
                mapStatus.put(symbolInfo.getCode(), new Status(symbolInfo.getCode(), false, ex.getMessage()));
            }
        }


//        List<SymbolDaily> items = downloadSymbolDaily(symbolInfo);
//        Query removeQuery = new Query(Criteria.where("code").is(symbolInfo.getCode()));
//        DeleteResult deleteResult = mongoTemplate.remove(removeQuery, SymbolDaily.class);
//        log.info("remove {} items of {} in symbol daily", deleteResult.getDeletedCount(), symbolInfo.getCode());
//        MongoBulkUtils.insertInBulk(mongoTemplate, 300, items, SymbolDaily.class);
//        log.info("inserted {} items of {} in symbol daily", items.size(), symbolInfo.getCode());
        return "";
    }

    private List<SymbolQuote> queryStockQuotes(SymbolInfo symbolInfo) {
        List<SymbolQuote> results = new ArrayList<>();
        Map<String, Object> body = new HashMap<>();
        body.put("code", symbolInfo.getCode());
        body.put("max_result", "");
        body.put("big", "1");
        body.put("seq", "");
        StockQuoteResponse response = lotteApiService.get(logId, appConf.getApiConnection().getStockQuotes(), StockQuoteResponse.class, body);
        if (response.isSuccess()) {
            if (response.getDataList() == null || response.getDataList().isEmpty()) {
                return results;
            }
            response.getDataList().forEach(item -> {
                if (item.getList() == null || item.getList().isEmpty()) {
                    return;
                }
                AtomicLong totalQuote = new AtomicLong(0);
                item.getList().forEach(it -> {
                    SymbolQuote symbolDaily = new SymbolQuote();
                    symbolDaily.setCode(symbolInfo.getCode());
                    symbolDaily.setType(symbolInfo.getType());
                    symbolDaily.setDate(new Date());
                    symbolDaily.setTime(it.getTime().replace(":", ""));
                    symbolDaily.setChange(it.getChange());
                    symbolDaily.setRate(it.getChangeRate());
                    symbolDaily.setMatchingVolume(it.getVolume());
                    symbolDaily.setTradingVolume(totalQuote.addAndGet(it.getVolume()));
                    symbolDaily.setLast(it.getLast());
                    symbolDaily.setRefCode(symbolInfo.getRefCode());
                    results.add(symbolDaily);
                });
            });
        } else {
            log.info("fail to query quotes {} {}", response.getErrorCode(), response.getErrorDesc());
        }
        return results;
    }

    private List<SymbolQuote> queryIndexQuotes(SymbolInfo symbolInfo) {
        List<SymbolQuote> results = new ArrayList<>();
        Map<String, Object> body = new HashMap<>();
        body.put("index", symbolInfo.getRefCode());
        body.put("max_result", 100);
        body.put("base_time", "");
        AtomicReference<String> nextData = new AtomicReference<>(null);
        while (true) {
            if (nextData.get() != null) {
                body.put("base_time", nextData.get());
            }
            IndexQuoteResponse response = lotteApiService.get(logId, appConf.getApiConnection().getIndexQuotes(), IndexQuoteResponse.class, body);
            if (response.isSuccess()) {
                if (response.getDataList() == null || response.getDataList().isEmpty()) {
                    return results;
                }
                response.getDataList().forEach(item -> {
                    if (item.getList() == null || item.getList().isEmpty()) {
                        return;
                    }
                    item.getList().forEach(it -> {
                        SymbolQuote symbolDaily = new SymbolQuote();
                        symbolDaily.setCode(symbolInfo.getCode());
                        symbolDaily.setTime(it.getTime().replace(":", ""));
                        symbolDaily.setDate(new Date());
                        symbolDaily.setType(symbolInfo.getType());
                        symbolDaily.setChange(it.getChange());
                        symbolDaily.setRate(it.getChangeRate());
                        symbolDaily.setMatchingVolume(it.getVolume());
                        symbolDaily.setLast(it.getLast());
                        symbolDaily.setRefCode(symbolInfo.getRefCode());
                        results.add(symbolDaily);
                    });
                    if (item.isHasNext()) {
                        nextData.set(item.getNextKey());
                    } else {
                        nextData.set(null);
                    }
                });
                if (nextData.get() == null) {
                    break;
                }
            } else {
                log.info("fail to query quotes {} {}", response.getErrorCode(), response.getErrorDesc());
            }
        }
        return results;
    }

    private List<SymbolQuote> queryCwQuotes(SymbolInfo symbolInfo) {
        List<SymbolQuote> results = new ArrayList<>();
        Map<String, Object> body = new HashMap<>();
        body.put("code", symbolInfo.getCode());
        body.put("max_result", 2000);
        AtomicReference<String> nextData = new AtomicReference<>(null);
        while (true) {
            if (nextData.get() != null) {
                body.put("seq", nextData.get());
            }
            CwQuoteResponse response = lotteApiService.get(logId, appConf.getApiConnection().getCwQuotes(), CwQuoteResponse.class, body);
            if (response.isSuccess()) {
                if (response.getDataList() == null || response.getDataList().isEmpty()) {
                    return results;
                }
                response.getDataList().forEach(item -> {
                    if (item.getList() == null || item.getList().isEmpty()) {
                        return;
                    }
                    item.getList().forEach(it -> {
                        SymbolQuote symbolDaily = new SymbolQuote();
                        symbolDaily.setCode(symbolInfo.getCode());
                        symbolDaily.setTime(it.getTime().replace(":", ""));
                        symbolDaily.setDate(new Date());
                        symbolDaily.setType(symbolInfo.getType());
                        symbolDaily.setChange(it.getChange());
                        symbolDaily.setRate(it.getChangeRate());
                        symbolDaily.setMatchingVolume(it.getVolume());
                        symbolDaily.setLast(it.getLast());
                        symbolDaily.setRefCode(symbolInfo.getRefCode());
                        results.add(symbolDaily);
                    });
                    if (item.isHasNext()) {
                        nextData.set(item.getNextKey());
                    } else {
                        nextData.set(null);
                    }
                });
                if (nextData.get() == null) {
                    break;
                }
            } else {
                log.info("fail to query quotes {} {}", response.getErrorCode(), response.getErrorDesc());
            }
        }
        return results;
    }

    private List<SymbolQuote> queryEtfQuotes(SymbolInfo symbolInfo) {
        List<SymbolQuote> results = new ArrayList<>();
        Map<String, Object> body = new HashMap<>();
        body.put("code", symbolInfo.getCode());
        body.put("max_result", 2000);
        AtomicReference<String> nextData = new AtomicReference<>(null);
        while (true) {
            if (nextData.get() != null) {
                body.put("seq", nextData.get());
            }
            EtfQuoteResponse response = lotteApiService.get(logId, appConf.getApiConnection().getEtfQuotes(), EtfQuoteResponse.class, body);
            if (response.isSuccess()) {
                if (response.getDataList() == null || response.getDataList().isEmpty()) {
                    return results;
                }
                response.getDataList().forEach(item -> {
                    if (item.getList() == null || item.getList().isEmpty()) {
                        return;
                    }
                    item.getList().forEach(it -> {
                        SymbolQuote symbolDaily = new SymbolQuote();
                        symbolDaily.setCode(symbolInfo.getCode());
                        symbolDaily.setTime(it.getTime().replace(":", ""));
                        symbolDaily.setDate(new Date());
                        symbolDaily.setType(symbolInfo.getType());
                        symbolDaily.setChange(it.getChange());
                        symbolDaily.setRate(it.getChangeRate());
                        symbolDaily.setMatchingVolume(it.getVolume());
                        symbolDaily.setLast(it.getLast());
                        symbolDaily.setRefCode(symbolInfo.getRefCode());
                        results.add(symbolDaily);
                    });
                    if (item.isHasNext()) {
                        nextData.set(item.getNextKey());
                    } else {
                        nextData.set(null);
                    }
                });
                if (nextData.get() == null) {
                    break;
                }
            } else {
                log.info("fail to query quotes {} {}", response.getErrorCode(), response.getErrorDesc());
            }
        }
        return results;
    }
}
