package com.techx.tradex.order.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.configurations.AppConf;
import com.techx.tradex.order.model.OrderMatchNotify;
import com.techx.tradex.order.services.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Properties;

@Slf4j
public class OrderMatchHandler extends KafkaConsumerHandler<OrderMatchNotify> {
    private final CacheService cacheService;

    @Autowired
    public OrderMatchHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            CacheService cacheService
    ) {
        TypeReference<Message<OrderMatchNotify>> typeReference = new TypeReference<>() {
        };
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getQuoteUpdate()),
                new Properties(), 5);
        this.cacheService = cacheService;
    }

    public void handle(Message<OrderMatchNotify> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        try {
            long t1 = System.currentTimeMillis();

            OrderMatchNotify orderMatchNotify = message.getData();
            cacheService.updateConditionalOrder(orderMatchNotify);

            log.info("{}", orderMatchNotify);
            long t2 = System.currentTimeMillis();
            log.info("accept orderMatchNotify take: {}", (t2 - t1));
        } catch (Exception e) {
            log.error("error while handle orderMatchNotify: {} _ {}", message, e);
        }
    }

}
