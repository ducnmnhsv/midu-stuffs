package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.market.model.v2.db.MarketStatus;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.MarketStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

public class MarketStatusUpdateHandler extends KafkaConsumerHandler<MarketStatus> {
    private static final Logger log = LoggerFactory.getLogger(MarketStatusUpdateHandler.class);

    private final MarketStatusService marketStatusService;


    public MarketStatusUpdateHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            MarketStatusService marketStatusService
    ) {
        this.marketStatusService = marketStatusService;
        TypeReference<Message<MarketStatus>> typeReference = new TypeReference<Message<MarketStatus>>() {
        };
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getMarketStatus()),
                new Properties(), appConf.getMaxThread(appConf.getTopics().getMarketStatus()));
    }

    public void handle(Message<MarketStatus> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        try {
            long t1 = System.currentTimeMillis();
            MarketStatus marketStatus = message.getData();
            marketStatusService.updateMarketStatus(marketStatus);
            long t2 = System.currentTimeMillis();
            log.info("accept marketStatus take: {}", (t2 - t1));
        } catch (Exception e) {
            log.error("error while handle marketStatus: {}", message.getMessageId(), e);
        }
    }

}
