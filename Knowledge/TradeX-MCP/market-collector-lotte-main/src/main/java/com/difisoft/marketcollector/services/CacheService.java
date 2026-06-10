package com.difisoft.marketcollector.services;

import com.difisoft.market.common.repository.SymbolInfoRepository;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.realtime.TransformData;
import com.difisoft.marketcollector.repositories.MarketRepository;
import com.difisoft.model.utils.DefaultUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Data
public class CacheService {
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private AppConf appConf;
    private SymbolInfoRepository symbolInfoRepo;
    private MarketRepository marketRepo;

    public CacheService(AppConf appConf, SymbolInfoRepository symbolInfoRepo,
                        MarketRepository marketRepo) {
        this.appConf = appConf;
        this.symbolInfoRepo = symbolInfoRepo;
        this.marketRepo = marketRepo;
    }

    private boolean isMarketOpen = true;
    private ConcurrentHashMap<String, String> refIndexCodeMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> futuresCodeRefMap = new ConcurrentHashMap<>();
    private Map<String, SymbolInfo> mapSymbolInfo = new HashMap<>();
    private Calendar timeStartReceiveBidAsk = Calendar.getInstance(); // start time to store bidOfferList from getTbl
    private Calendar timeStopReceiveBidAsk = Calendar.getInstance(); // start time to store bidOfferList from getTbl

    public void reset() {
        timeStartReceiveBidAsk = Calendar.getInstance();
        String[] hourMinute = appConf.getTimeStartReceiveBidAsk().split(":");
        timeStartReceiveBidAsk.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourMinute[0]));
        timeStartReceiveBidAsk.set(Calendar.MINUTE, Integer.parseInt(hourMinute[1]));

        timeStopReceiveBidAsk = Calendar.getInstance();
        hourMinute = appConf.getTimeStopReceiveBidAsk().split(":");
        timeStopReceiveBidAsk.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourMinute[0]));
        timeStopReceiveBidAsk.set(Calendar.MINUTE, Integer.parseInt(hourMinute[1]));

        refIndexCodeMap.clear();
        futuresCodeRefMap.clear();

        Sort sortByCode = Sort.by(Sort.Direction.ASC, "code");
        log.info("reset securitiesInfo ...");
        mapSymbolInfo.clear();
        List<SymbolInfo> securitiesInfoList = this.symbolInfoRepo.findAll(sortByCode);
        this.resetFromList(securitiesInfoList);
    }

    @SafeVarargs
    public final void resetFromList(List<SymbolInfo>... securitiesInfoLists) {
        for (List<SymbolInfo> list : securitiesInfoLists) {
            list.forEach(symbolInfo -> {
                mapSymbolInfo.put(symbolInfo.getCode(), symbolInfo);
                if (symbolInfo.getType() == null) {
                    log.warn("symbol doesn't have type {}", symbolInfo);
                    return;
                }
                if (symbolInfo.getType().equals(SymbolTypeEnum.FUTURES)) {
                    if (symbolInfo.getCode() != null && symbolInfo.getRefCode() != null) {
                        this.futuresCodeRefMap.put(symbolInfo.getCode(), symbolInfo.getRefCode());
                    }
                }
                if (symbolInfo.getType().equals(SymbolTypeEnum.INDEX) && symbolInfo.getRefCode() != null) {
                    this.refIndexCodeMap.put(symbolInfo.getRefCode(), symbolInfo.getCode());
                }
            });
        }
        log.info("finished reset cache!");
    }

    public void init() {
        this.reset();
        TransformData.setAppConf(appConf);
    }

}
