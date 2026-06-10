package com.difisoft.marketcollector.consumers;


import com.difisoft.job.JobHandler;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.services.ISymbolInfoService;
import com.difisoft.marketcollector.services.RealTimeDataListenerService;
import com.difisoft.marketcollector.services.ISymbolInfoService;
import com.difisoft.marketcollector.services.SystemService;
import com.difisoft.marketcollector.services.recover.DailyRecoverByApiService;
import com.difisoft.marketcollector.services.recover.DailyRecoverService;
import com.difisoft.marketcollector.services.recover.QuoteRecoverService;
import com.difisoft.marketcollector.services.recover.QuoteStatisticService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestHandler extends JobHandler {
    public static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    @Autowired
    public RequestHandler(
            ObjectMapper objectMapper,
            ISymbolInfoService symbolInfoService,
            AppConf appConf,
            RealTimeDataListenerService realTimeDataListenerService,
            DailyRecoverService dailyRecoverService,
            DailyRecoverByApiService dailyRecoverByApiService,
            QuoteRecoverService quoteRecoverService,
            QuoteStatisticService quoteStatisticService,
            SystemService systemService
    ) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), "1", 5);
        this.uriBuilder().
                add("job:/api/v1/collector/downloadSymbol", Object.class, symbolInfoService::downloadSymbolFromRequest).
                add("job:/api/v1/collector/quoteDataRecover", String.class, quoteRecoverService::downloadFromRequest).
                add("job:/api/v1/collector/dailyDataRecover", Object.class, dailyRecoverService::downloadFromRequest).
                add("job:/api/v1/collector/dailyDataRecoverByApi", DailyRecoverByApiService.DailyApiRecoverRequest.class, dailyRecoverByApiService::recover).
                add("job:/api/v1/collector/dailyDataRecoverNoCache", Object.class, dailyRecoverService::downloadFromRequestNoCache).
                add("job:/api/v1/collector/quoteStatistic", Object.class, quoteStatisticService::getStatistic).
                add("job:/api/v1/collector/forceDownloadSymbol", Object.class, symbolInfoService::forceDownloadSymbolFromRequest).
                add("job:/api/v1/collector/realtime/start", Object.class, (r, c) -> {
                    realTimeDataListenerService.run();
                    return null;
                }).
                add("job:/api/v1/collector/realtime/stop", Object.class, (r, c) -> {
                    realTimeDataListenerService.stop();
                    return null;
                }).
                add("job:/api/v1/collector/exit", Object.class, (r, c) -> {
                    systemService.shutdown();
                    return null;
                }).
                end();
    }
}
