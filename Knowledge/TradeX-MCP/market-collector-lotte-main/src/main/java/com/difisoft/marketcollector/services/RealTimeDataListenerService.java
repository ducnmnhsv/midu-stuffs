package com.difisoft.marketcollector.services;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.services.realtime.RealTimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;


@Service
public class RealTimeDataListenerService extends RealTimeService {

    public RealTimeDataListenerService(
            AppConf appConf,
            CacheService cacheService,
            MonitorService monitorService,
            MarketRedisDao marketRedisDao,
            HtsConnectionService htsConnectionService,
            DownloadSymbolListService downloadSymbolListService,
            ObjectMapper objectMapper
    ) {
        super(appConf, cacheService, monitorService, marketRedisDao,
                htsConnectionService, downloadSymbolListService, objectMapper);
    }
}
