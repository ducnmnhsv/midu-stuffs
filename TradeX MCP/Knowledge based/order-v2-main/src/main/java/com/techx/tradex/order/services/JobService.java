package com.techx.tradex.order.services;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Data
public class JobService {
    private static final Logger log = LoggerFactory.getLogger(JobService.class);
    private CacheService cacheService;
    private TrailingOrderService trailingOrderService;
    private StopOrderService stopOrderService;

    @Autowired
    public JobService(CacheService cacheService, TrailingOrderService trailingOrderService,
                      StopOrderService stopOrderService) {
        this.cacheService = cacheService;
        this.trailingOrderService = trailingOrderService;
        this.stopOrderService = stopOrderService;
    }

    // Internal Job for Project
    @Scheduled(cron = "${app.schedulers.resetCache}")
    public void resetCache() {
        cacheService.reset();
    }

    //@Scheduled(cron = "${app.schedulers.updateTrailingOrderToDatabase}")
    public void updateTrailingOrderToDatabase() {
        cacheService.updateTrailingOrderToDatabase();
    }


    @Scheduled(cron = "${app.schedulers.cancelAllTrailingOrder}")
    public void cancelAllTrailingOrder() {
        trailingOrderService.cancelAllBySchedule();
    }


    @Scheduled(cron = "${app.schedulers.cancelAllStopOrder}")
    public void cancelAllStopOrder() {
        stopOrderService.cancelAllBySchedule();
    }

}
