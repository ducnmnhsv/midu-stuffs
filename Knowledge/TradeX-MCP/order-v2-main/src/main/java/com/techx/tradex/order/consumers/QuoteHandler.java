package com.techx.tradex.order.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.market.model.v2.db.SymbolQuote;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.configurations.AppConf;
import com.techx.tradex.order.services.CacheService;
import com.techx.tradex.order.services.OrderTriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Properties;

@Service
public class QuoteHandler extends KafkaConsumerHandler<SymbolQuote> {
    private static final Logger log = LoggerFactory.getLogger(QuoteHandler.class);

    private final CacheService cacheService;
    private final OrderTriggerService orderTriggerService;

    @Autowired
    public QuoteHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            CacheService cacheService,
            OrderTriggerService orderTriggerService
    ) {
        this.cacheService = cacheService;
        this.orderTriggerService = orderTriggerService;
        if (appConf.getMode() == AppConf.Mode.QUERY) {
            return;
        }
        TypeReference<Message<SymbolQuote>> typeReference = new TypeReference<>() {
        };
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getQuoteUpdate()),
                new Properties(), 1);
    }

    public void handle(Message<SymbolQuote> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        try {
            long t1 = System.currentTimeMillis();
            SymbolQuote symbolQuote = message.getData();

            log.info("{}", symbolQuote);
            cacheService.updateCacheByQuote(symbolQuote);
            orderTriggerService.receiveQuote(symbolQuote);
            long t2 = System.currentTimeMillis();
            log.info("accept futuresQuote take: {}", (t2 - t1));
        } catch (Exception e) {
            log.error("error while handle futuresQuote: {}", message, e);
        }
    }

}
