package com.difisoft.marketcollector.services.recover;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.utils.MongoBulkUtils;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolDaily;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.lotte.api.IndexDailyRequest;
import com.difisoft.marketcollector.model.lotte.api.IndexDailyResponse;
import com.difisoft.marketcollector.model.lotte.api.IndexListRequest;
import com.difisoft.marketcollector.model.lotte.api.IndexListResponse;
import com.difisoft.marketcollector.model.lotte.api.StockDailyResponse;
import com.difisoft.marketcollector.services.LotteApiService;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.MultiThreadCompletablePool;
import com.mongodb.client.result.DeleteResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Service
@Slf4j
public class DailyRecoverByApiService {
    private static final String logId = DailyRecoverByApiService.class.getSimpleName();

    private final AppConf appConf;
    private final MarketRedisDao marketRedisDao;
    private final LotteApiService lotteApiService;
    private final MongoTemplate mongoTemplate;
    private Map<String, IndexListResponse.Item> indexMap = new HashMap<>();


    public DailyRecoverByApiService(
            AppConf appConf,
            MarketRedisDao marketRedisDao,
            LotteApiService lotteApiService,
            MongoTemplate mongoTemplate
    ) {
        this.appConf = appConf;
        this.marketRedisDao = marketRedisDao;
        this.lotteApiService = lotteApiService;
        this.mongoTemplate = mongoTemplate;
    }

    public CompletableFuture<String> recover(DailyApiRecoverRequest request, RequestContext<DailyApiRecoverRequest> ctx) {
        CompletableFuture<String> result = new CompletableFuture<>();
        List<SymbolInfo> symbolInfoList = marketRedisDao.getAllSymbolInfo();
        List<Supplier<String>> suppliers = symbolInfoList.stream()
                .filter(s -> {
                    if (request == null || request.getSymbols() == null || request.getSymbols().isEmpty()) {
                        return true;
                    }
                    return request.getSymbols().contains(s.getCode()) || request.getSymbols().contains(s.getCode().toUpperCase());
                })
                .map(s -> (Supplier<String>) () -> updateSymbolDaily(ctx.getId(), s)).toList();
        if (suppliers.isEmpty()) {
            result.complete("finish update daily. no symbol match in the list. please check the symbol code again");
            return result;
        }
        MultiThreadCompletablePool<String> pool = new MultiThreadCompletablePool<>(
                suppliers,
                Math.min(25, symbolInfoList.size())
        );
        int totalList = (request == null || request.getSymbols() == null || request.getSymbols().isEmpty())
                ? symbolInfoList.size()
                : request.getSymbols().size();
        pool.execute().thenAccept(r -> {
            log.error("{} finish update daily. success {} / total {}", logId, pool.getSuccessResults().size(), totalList);
            result.complete(String.format("%s finish update daily. success %d/total %d", logId, pool.getSuccessResults().size(), totalList));
        });
        return result;
    }

    private String updateSymbolDaily(String logId, SymbolInfo symbolInfo) {
        try {
            log.info("{} start query {}", logId, symbolInfo.getCode());
            List<SymbolDaily> items = downloadSymbolDaily(logId, symbolInfo);
            Query removeQuery = new Query(Criteria.where("code").is(symbolInfo.getCode()));
            DeleteResult deleteResult = mongoTemplate.remove(removeQuery, SymbolDaily.class);
            log.info("{} remove {} items of {} in symbol daily", logId, deleteResult.getDeletedCount(), symbolInfo.getCode());
            MongoBulkUtils.insertInBulk(mongoTemplate, 300, items, SymbolDaily.class);
            log.info("{} inserted {} items of {} in symbol daily", logId, items.size(), symbolInfo.getCode());
        } catch (Exception e) {
            log.error("{} fail to update symbolDaily {}", logId, symbolInfo.getCode(), e);
        }
        return "";
    }

    private List<SymbolDaily> downloadSymbolDaily(String logId, SymbolInfo symbolInfo) {
        if (symbolInfo.getType() == SymbolTypeEnum.INDEX) {
            return downloadIndexDaily(logId, symbolInfo);
        }
        List<SymbolDaily> results = new ArrayList<>();
        Map<String, Object> body = new HashMap<>();
        body.put("code", symbolInfo.getCode());
        body.put("max_result", appConf.getApiConnection().getStockDailySize() + "");
        body.put("price_tp", "1");
        AtomicReference<String> nextData = new AtomicReference<>(null);
        while (true) {
            if (nextData.get() != null) {
                body.put("next_key", nextData.get());
            }
            StockDailyResponse response = lotteApiService.get(logId, appConf.getApiConnection().getStockDaily(), StockDailyResponse.class, body);
            if (response.isSuccess()) {
                if (response.getDataList() == null || response.getDataList().isEmpty()) {
                    break;
                }
                response.getDataList().forEach(item -> {
                    if (item.getList() == null || item.getList().isEmpty()) {
                        return;
                    }
                    item.getList().forEach(it -> {
                        SymbolDaily symbolDaily = new SymbolDaily();
                        symbolDaily.setId(String.format("%s_%s", symbolInfo.getCode(), it.getDate()));
                        symbolDaily.setCode(symbolInfo.getCode());
                        symbolDaily.setDate(Date.from(DefaultUtils.parseZonedDate(it.getDate()).toInstant()));

                        symbolDaily.setType(symbolInfo.getType());
                        symbolDaily.setMarketType(symbolInfo.getMarketType());
                        symbolDaily.setChange(it.getChange());
                        symbolDaily.setRate(it.getChangeRate());
                        symbolDaily.setTradingVolume(it.getVolume());
                        symbolDaily.setTradingValue(it.getValue());
                        symbolDaily.setOpen(it.getOpen());
                        symbolDaily.setHigh(it.getHigh());
                        symbolDaily.setLow(it.getLow());
                        symbolDaily.setLast(it.getLast());
//                            symbolDaily.setReferencePrice((double)it.getChange().getValue());
                        symbolDaily.setRefCode(symbolInfo.getRefCode());
                        symbolDaily.setCreatedAt(symbolDaily.getDate());
                        symbolDaily.setUpdatedAt(new Date());
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
                log.info("{} fail to query index daily {} {}", logId, response.getErrorCode(), response.getErrorDesc());
                break;
            }
        }
        return results;
    }



    private void downloadIndexList(String id, Map<String, IndexListResponse.Item> map, String nextKey) {
        IndexListResponse response = lotteApiService.get(id, appConf.getApiConnection().getIndexListApi(),
                IndexListResponse.class, new IndexListRequest(nextKey), log);
        AtomicReference<String> next = new AtomicReference<>(null);
        response.getDataList().forEach(it -> {
            if (it.isHasNext()) {
                next.set(it.getNextKey());
            } else {
                next.set(null);
            }
            it.getList().forEach(item -> map.put(item.getCode(), item));
        });
        if (next.get() != null) {
            downloadIndexList(id, map, next.get());
        }
    }

    private List<SymbolDaily> downloadIndexDaily(String logId, SymbolInfo symbolInfo) {
        if (indexMap.isEmpty()) {
            downloadIndexList(logId, indexMap, null);
        }

        List<SymbolDaily> results = new ArrayList<>();
        String refCode = symbolInfo.getRefCode();
        if (refCode == null || refCode.isEmpty()) {
            refCode = indexMap.get(symbolInfo.getCode()).getCode();
        }
        AtomicReference<String> nextData = new AtomicReference<>(null);
        while (true) {
            IndexDailyRequest request = IndexDailyRequest.builder()
                    .rowCount(appConf.getApiConnection().getIndexDailySize())
                    .indexRefCode(refCode)
                    .nextData(nextData.get())
                    .build();
            IndexDailyResponse response = lotteApiService.get(logId, appConf.getApiConnection().getIndexDaily(),
                    IndexDailyResponse.class, request, log);

            if (response.isSuccess()) {
                if (response.getDataList() == null || response.getDataList().isEmpty()) {
                    break;
                }
                response.getDataList().forEach(item -> {
                    if (item.getList() == null || item.getList().isEmpty()) {
                        nextData.set(null);
                        return;
                    }
                    item.getList().forEach(it -> {
                        if (it.getDate().equals("00000000")) {
                            return;
                        }
                        SymbolDaily symbolDaily = new SymbolDaily();
                        symbolDaily.setId(String.format("%s_%s", symbolInfo.getCode(), it.getDate()));
                        symbolDaily.setCode(symbolInfo.getCode());
                        symbolDaily.setDate(Date.from(DefaultUtils.parseZonedDate(it.getDate()).toInstant()));
                        symbolDaily.setType(symbolInfo.getType());
                        symbolDaily.setMarketType(symbolInfo.getMarketType());
                        symbolDaily.setChange(it.getChange());
                        symbolDaily.setRate(it.getChangeRate());
                        symbolDaily.setTradingVolume(it.getVolume());
                        symbolDaily.setTradingValue(it.getValue() * 1000000);
                        symbolDaily.setOpen(it.getOpen());
                        symbolDaily.setHigh(it.getHigh());
                        symbolDaily.setLow(it.getLow());
                        symbolDaily.setLast(it.getClose());
//                            symbolDaily.setReferencePrice((double)it.getChange().getValue());
                        symbolDaily.setRefCode(symbolInfo.getRefCode());
                        symbolDaily.setCreatedAt(Date.from(DefaultUtils.parseZonedDate(it.getDate()).toInstant()));
                        symbolDaily.setUpdatedAt(symbolDaily.getCreatedAt());
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
                log.info("{} fail to query index daily {} {}", logId, response.getErrorCode(), response.getErrorDesc());
                break;
            }
        }
        return results;
    }

    @Data
    public static class DailyApiRecoverRequest {
        private Set<String> symbols;
    }
}
