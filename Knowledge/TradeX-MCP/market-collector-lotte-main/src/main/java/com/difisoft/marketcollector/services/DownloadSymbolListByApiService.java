package com.difisoft.marketcollector.services;

import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.db.Symbol;
import com.difisoft.marketcollector.model.lotte.api.IndexListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class DownloadSymbolListByApiService {

    private final LotteApiService lotteApiService;
    private final AppConf appConf;


    public DownloadSymbolListByApiService(LotteApiService lotteApiService, AppConf appConf) {
        this.lotteApiService = lotteApiService;
        this.appConf = appConf;
    }

    private void queryListIndex(List<Symbol> symbols, String logId, String nextData) {
        Map<String, String> body = new HashMap<>();
        body.put("mkt_tp", "%");
        if (nextData != null) {
            body.put("next_data", nextData);
        }
        IndexListResponse indexListResponse = lotteApiService.get(logId, appConf.getApiConnection().getIndexListApi(), IndexListResponse.class, body);
        AtomicReference<String> newNextData = new AtomicReference<>();
        if (indexListResponse.isSuccess()) {
            indexListResponse.getDataList().forEach(item -> {
                item.getList().forEach(it -> {
                    Symbol symbol = new Symbol();
                    symbol.setCode(it.getSymbol().replace("INDEX", ""));
                    symbol.setSecCode(it.getCode());
                    symbol.setRefCode(it.getCode());
                    symbol.setExchange("HSX".equals(it.getExchange()) ? "HOSE" : it.getExchange().name());
                    symbol.setName(it.getVietnameseName());
                    symbol.setNameEn(it.getEnglishName());
                    symbol.setMarketType(symbol.getExchange());
                    symbol.setType(SymbolTypeEnum.INDEX);
                    symbol.setSecuritiesType(SymbolTypeEnum.INDEX.name());
                    symbol.setIsHighlight(1000);
                });
                if (item.isHasNext()) {
                    newNextData.set(item.getNextKey());
                }
            });
            if (newNextData.get() != null) {
                queryListIndex(symbols, logId, newNextData.get());
            }
        } else {
            log.error("fail to query index list {}", indexListResponse);
        }
    }


    private void queryStocksByIndex(List<Symbol> symbols, String logId, String nextData) {
        Map<String, String> body = new HashMap<>();
        body.put("mkt_tp", "%");
        if (nextData != null) {
            body.put("next_data", nextData);
        }
        IndexListResponse indexListResponse = lotteApiService.get(logId, appConf.getApiConnection().getIndexListApi(), IndexListResponse.class, body);
        AtomicReference<String> newNextData = new AtomicReference<>();
        if (indexListResponse.isSuccess()) {
            indexListResponse.getDataList().forEach(item -> {
                item.getList().forEach(it -> {
                    Symbol symbol = new Symbol();
                    symbol.setCode(it.getSymbol());
                    symbol.setSecCode(it.getCode());
                    symbol.setRefCode(it.getCode());
                    symbol.setExchange("HSX".equals(it.getExchange()) ? "HOSE" : it.getExchange().name());
                    symbol.setName(it.getVietnameseName());
                    symbol.setNameEn(it.getEnglishName());
                    symbol.setMarketType(symbol.getExchange());
                    symbol.setType(SymbolTypeEnum.INDEX);
                    symbol.setSecuritiesType(SymbolTypeEnum.INDEX.name());
                    symbol.setIsHighlight(1000);
                });
                if (item.isHasNext()) {
                    newNextData.set(item.getNextKey());
                }
            });
            if (newNextData.get() != null) {
                queryListIndex(symbols, logId, newNextData.get());
            }
        } else {
            log.error("fail to query index list {}", indexListResponse);
        }
    }
}
