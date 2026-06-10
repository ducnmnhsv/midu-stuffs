package com.difisoft.marketcollector.job;

import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.services.*;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Data
public class JobService {
    private static final Logger log = LoggerFactory.getLogger(JobService.class);
    private CacheService cacheService;
    private RealTimeDataListenerService realTimeDataListenerService;
    private AppConf appConf;
    private ISymbolInfoService symbolInfoService;
    private RiseFallStockRankService riseFallStockRankService;
    private RightInfoService rightInfoService;

    public JobService(
            CacheService cacheService,
            RealTimeDataListenerService realTimeDataListenerService,
            AppConf appConf,
            RiseFallStockRankService riseFallStockRankService,
            ISymbolInfoService symbolInfoService,
            RightInfoService rightInfoService
    ) {
        this.cacheService = cacheService;
        this.realTimeDataListenerService = realTimeDataListenerService;
        this.appConf = appConf;
        this.symbolInfoService = symbolInfoService;
        this.riseFallStockRankService = riseFallStockRankService;
        this.rightInfoService = rightInfoService;
    }

    @Scheduled(cron = "${app.schedulers.startRealtime1st}")
    public void startRealtime1st() {
        realTimeDataListenerService.run();
    }

    @Scheduled(cron = "${app.schedulers.stopRealtime1st}")
    public void stopRealtime1st() {
        realTimeDataListenerService.stop();
    }

    @Scheduled(cron = "${app.schedulers.startRealtime2nd}")
    public void startRealtime2nd() {
        realTimeDataListenerService.run();
    }

    @Scheduled(cron = "${app.schedulers.stopRealtime2nd}")
    public void stopRealtime2nd() {
        realTimeDataListenerService.stop();
    }

    @Scheduled(cron = "${app.schedulers.downloadSymbol}")
    public void downloadSymbol() {
        symbolInfoService.downloadSymbol("JobScheduler");
    }

    @Scheduled(cron = "${app.schedulers.riseFallStockRankSave}")
    public void riseFallStockRankSave() {
        if (appConf.isEnableQuery()) {
            riseFallStockRankService.getAndSaveStockRanking();
        }
    }

    @Scheduled(cron = "${app.schedulers.rightInfoSave}")
    public void rightInfoSave() {
        if (appConf.isEnableQuery()) {
            rightInfoService.getAndSaveRightInfo();
        }
    }
}
