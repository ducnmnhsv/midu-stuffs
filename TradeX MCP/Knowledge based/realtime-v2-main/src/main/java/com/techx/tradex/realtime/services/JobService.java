package com.techx.tradex.realtime.services;

import com.difisoft.kafka.handler.RequestContext;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.constants.Constants;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Data
public class JobService {
    private static final Logger log = LoggerFactory.getLogger(JobService.class);
    private RedisService redisService;
    private HolidayService holidayService;
    private AppConf appConf;
    private SymbolInfoRollerService symbolInfoRollerService;
    private ThemeService themeService;
    private QuoteService quoteService;
    private final SymbolInfoService symbolInfoService;
    private final CacheService cacheService;

    @Autowired
    public JobService(
            AppConf appConf
            , RedisService redisService
            , HolidayService holidayService
            , SymbolInfoRollerService symbolInfoRollerService
            , ThemeService themeService
            , SymbolInfoService symbolInfoService
            , QuoteService quoteService
            , CacheService cacheService
    ) {
        this.appConf = appConf;
        this.redisService = redisService;
        this.holidayService = holidayService;
        this.symbolInfoRollerService = symbolInfoRollerService;
        this.themeService = themeService;
        this.symbolInfoService = symbolInfoService;
        this.quoteService = quoteService;
        this.cacheService = cacheService;
    }

    @Scheduled(cron = "${app.schedulers.refreshSymbolInfo}")
    public void refreshSymbolInfo() {
        boolean isHoliday = holidayService.isHolidayOrWeekend();
        log.info("_______________________refreshSymbolInfo by Job {}", isHoliday);
        if (isHoliday) {
            log.info("========== TODAY IS HOLIDAY OR WEEKEND - END refreshSymbolInfo =======");
            return;
        }
        redisService.refreshSymbolInfo();
    }

    @Scheduled(cron = "${app.schedulers.clearSymbolDaily}")
    public void clearSymbolDaily() {
        boolean isHoliday = holidayService.isHolidayOrWeekend();
        log.info("_______________________clearSymbolDaily by Job {}", isHoliday);
        if (isHoliday) {
            log.info("========== TODAY IS HOLIDAY OR WEEKEND - END clearSymbolDaily =======");
            return;
        }
        redisService.clearOldSymbolDaily();
    }

    @Scheduled(cron = "${app.schedulers.removeAutoData}")
    public void removeAutoData() {
        boolean isHoliday = holidayService.isHolidayOrWeekend();
        log.info("_______________________removeAutoData by Job {}", isHoliday);
        if (isHoliday) {
            log.info("========== TODAY IS HOLIDAY OR WEEKEND - END removeAutoData =======");
            return;
        }
        redisService.removeAutoData();
        cacheService.reset();
    }

    @Scheduled(cron = "${app.schedulers.saveRedisToDatabase}")
    public void saveRedisToDatabase() {
        log.info("_______________________saveRedisToDatabase by Job");
        redisService.saveRedisToDatabase(appConf.isEnableSaveQuote(), appConf.isEnableSaveQuoteMinute(), appConf.isEnableSaveBidAsk());
    }


    @Scheduled(cron = "${app.schedulers.restartService}")
    public void shutdown() {
        log.info("_______________________restartService by Job");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.exit(0);
    }


    @Scheduled(cron = "${app.schedulers.rollerSymbolInfo}")
    public void rollerSymbolInfo() {
        log.info("_______________________rollerSymbolInfo by Job {}", appConf.isEnableRoller());
        if (appConf.isEnableRoller()) {
            this.symbolInfoRollerService.rollerData();
        }
    }

    @Scheduled(cron = "${app.schedulers.updateThemeStatistic}")
    public void updateThemeStatistic() {
        boolean isHoliday = holidayService.isHolidayOrWeekend();
        log.info("_______________________updateThemeStatistic by Job {}", isHoliday);
        if (isHoliday) {
            log.info("========== TODAY IS HOLIDAY OR WEEKEND - END removeAutoData =======");
            return;
        }
        themeService.updateThemeStatistic();
    }

    @Scheduled(cron = "${app.schedulers.stockTopWorstReturns}")
    private void stockTopWorstReturnsNotificationJob() {
        boolean isHoliday = holidayService.isHolidayOrWeekend();
        log.info("_______________________stockTopWorstReturnsNotificationJob by Job {}", isHoliday);
        if (isHoliday) {
            log.info("========== TODAY IS HOLIDAY OR WEEKEND - END stockTopWorstReturnsNotificationJob =======");
            return;
        }
        LocalDateTime startTime = LocalDateTime.now();
        String jobID = String.format("stock_top_worst_returns_notification_job__%s"
                , startTime.format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT_yyyyMMddHHmmss)));
        log.info("==== [stockTopWorstReturnsNotificationJob__{}] START =====", jobID);
        symbolInfoService.stockTopWorstReturnsInfoExecute(null, new RequestContext<>(jobID, null));
        log.info("==== [stockTopWorstReturnsNotificationJob__{}] END: took {} milliseconds ====="
                , jobID, Duration.between(startTime, LocalDateTime.now()).toMillis()
        );
    }

    @Scheduled(cron = "0 0 * * * *")
    private void setStartCurrentDate() {
        quoteService.setStartCurrentDate();
    }
}
