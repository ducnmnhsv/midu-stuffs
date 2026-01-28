package com.difisoft.marketcollector.services;

import com.difisoft.htsconnection.socket.message.receive.GeneratedClassRegistration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class StartupService implements ApplicationRunner {
    private final ObjectMapper objectMapper;
    private final RealTimeDataListenerService realTimeDataListenerService;
    private final CacheService cacheService;

    public StartupService(
            ObjectMapper objectMapper,
            RealTimeDataListenerService realTimeDataListenerService,
            CacheService cacheService
    ) {
        this.objectMapper = objectMapper;
        this.realTimeDataListenerService = realTimeDataListenerService;
        this.cacheService = cacheService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        GeneratedClassRegistration.register();
        this.cacheService.init();
        this.realTimeDataListenerService.run();
    }
}
