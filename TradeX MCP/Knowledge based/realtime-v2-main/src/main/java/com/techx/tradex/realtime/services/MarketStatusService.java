package com.techx.tradex.realtime.services;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.model.v2.db.MarketStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MarketStatusService {

    private MarketRedisDao marketRedisDao;
    private CacheService cacheService;

    @Autowired
    public MarketStatusService(MarketRedisDao marketRedisDao, CacheService cacheService) {
        this.marketRedisDao = marketRedisDao;
        this.cacheService = cacheService;
    }

    public void updateMarketStatus(MarketStatus marketStatus) {
        marketStatus.setId(marketStatus.getMarket() + "_" + marketStatus.getType());
        marketStatus.setDate(new Date());
        marketRedisDao.setMarketStatus(marketStatus);

        String session = (!"ATO".equals(marketStatus.getStatus()) && !"ATC".equals(marketStatus.getStatus()))
                ? null : marketStatus.getStatus();
        cacheService.getMapSymbolInfo().forEach((s, symbolInfo) -> {
            if (symbolInfo.getMarketType() != null && symbolInfo.getMarketType().equals(marketStatus.getMarket())) {
                symbolInfo.setSessions(session);
                marketRedisDao.setSymbolInfo(symbolInfo);
            }
        });
    }
}
