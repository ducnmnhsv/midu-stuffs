package com.techx.tradex.realtime.services;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.kafka.model.RequestHandlerMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.consumers.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StartupService implements ApplicationRunner {
    private final MonitorService monitorService;
    private final ObjectMapper objectMapper;
    private final AppConf appConf;
    private final CacheService cacheService;
    private final ThemeService themeService;
    private final MarketStatusService marketStatusService;
    private final InitService initService;
    private final SymbolInfoService symbolInfoService;


    public StartupService(
            MonitorService monitorService,
            ObjectMapper objectMapper,
            AppConf appConf,
            CacheService cacheService,
            ThemeService themeService,
            MarketStatusService marketStatusService,
            InitService initService,
            SymbolInfoService symbolInfoService
    ) {
        this.monitorService = monitorService;
        this.objectMapper = objectMapper;
        this.appConf = appConf;
        this.cacheService = cacheService;
        this.themeService = themeService;
        this.marketStatusService = marketStatusService;
        this.initService = initService;
        this.symbolInfoService = symbolInfoService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("init cache");
        cacheService.init();
        log.info("init processors");
        monitorService.init();
//        monitorService.doRecoverQuoteMinute("NVL", new RequestContext<>("startup", new RequestHandlerMessage<>()));
//        monitorService.doMergeWrongOrderQuote("SBV", new RequestContext<>("startup", new RequestHandlerMessage<>()));
        log.info("init kafka clients");
        new MarketStatusUpdateHandler(objectMapper, appConf, marketStatusService);
        new SymbolInfoUpdateHandler(objectMapper, appConf, initService, symbolInfoService);
        new MarketInitHandler(objectMapper, appConf, cacheService, themeService);
        new QuoteUpdateHandler(objectMapper, appConf, monitorService);
        new ExtraQuoteUpdateHandler(objectMapper, appConf, monitorService);
        new BidOfferUpdateHandler(objectMapper, appConf, monitorService);
        new AdvertisedUpdateHandler(objectMapper, appConf, monitorService);
        new BidOfferOddLotUpdateHandler(objectMapper, appConf, monitorService);
        new DealNoticeUpdateHandler(objectMapper, appConf, monitorService);
        new IndexStockListUpdateHandler(objectMapper, appConf, monitorService);
        new QuoteOddLotUpdateHandler(objectMapper, appConf, monitorService);
        new QuoteRecoverHandler(objectMapper, appConf, monitorService);
    }
}
