package com.techx.tradex.notification.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    @Scheduled(cron = "${app.schedulers.shutdown}")
    public void shutdown() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
