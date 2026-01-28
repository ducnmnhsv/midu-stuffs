package com.difisoft.marketcollector.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SystemService {
    private static final Logger log = LoggerFactory.getLogger(SystemService.class);

    @org.springframework.scheduling.annotation.Async
    public void shutdown() {
        log.warn("shutdown system in 5 seconds");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
