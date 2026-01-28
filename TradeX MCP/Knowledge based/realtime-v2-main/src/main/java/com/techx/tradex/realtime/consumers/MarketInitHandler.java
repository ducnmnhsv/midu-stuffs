package com.techx.tradex.realtime.consumers;


import com.difisoft.kafka.consumer.ConsumerHandler;
import com.difisoft.kafka.consumer.ThreadedKafkaConsumer;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.CacheService;
import com.techx.tradex.realtime.services.ThemeService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class MarketInitHandler implements ConsumerHandler<String, String> {
    private static final Logger log = LoggerFactory.getLogger(MarketInitHandler.class);

    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    private final ThemeService themeService;
    private Thread timeoutThread;
    private AtomicBoolean shouldStopTimeoutThread = new AtomicBoolean(false);

    public MarketInitHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            CacheService cacheService,
            ThemeService themeService) {
        if (!appConf.isEnableInitData()) { // no listen this topic when this feature enable. it could cause the looping
            new ThreadedKafkaConsumer<>(appConf.getKafkaBootstraps(), appConf.getClusterId(),
                    Collections.singletonList(appConf.getTopics().getMarketInit()),
                    new Properties(), this, appConf.getMaxThread(appConf.getTopics().getMarketInit()));
        }
        this.objectMapper = objectMapper;
        this.cacheService = cacheService;
        this.themeService = themeService;
    }


    @Override
    public void handle(ConsumerRecord<String, String> consumerRecord) {
        Message<?> message;
        String value = consumerRecord.value();
        try {
            message = this.objectMapper.readValue(value, Message.class);
        } catch (IOException e) {
            log.error("fail to decode message {}", value, e);
            return;
        }

        try {
            if (message.getUri().equals("/startInit")) {
                if (!cacheService.isEnableAutoData()) {
                    if (timeoutThread != null && timeoutThread.isAlive()) {
                        shouldStopTimeoutThread.set(true);
                    }
                }
                cacheService.pauseAutoData();
                Runnable task2 = () -> {
                    for (int i = 0; i < 20; i++) {
                        try {
                            Thread.sleep(100);  // timeout for startGetTbl. resuming saving auto data
                        } catch (Exception ignore) {
                        }
                        if (shouldStopTimeoutThread.get()) {
                            return;
                        }
                    }
                    if (!cacheService.isEnableAutoData()) {
                        log.info("auto resumeAutoDate cause of timeout");
                        cacheService.reset();
                        themeService.updateThemeStatistic();
                        cacheService.resumeAutoData();
                    }
                };
                timeoutThread = new Thread(task2);
                timeoutThread.start();
            } else if (message.getUri().equals("/endInit")) {
                if (!cacheService.isEnableAutoData()) {
                    cacheService.reset();
                    themeService.updateThemeStatistic();
                    cacheService.resumeAutoData();
                }
            }
        } catch (Exception e) {
            log.error("fail to handle request {}", value, e);
        }
    }
}
