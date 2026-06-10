package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.market.model.v2.db.SymbolQuote;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.model.request.QuoteRecover;
import com.techx.tradex.realtime.services.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Properties;

@Slf4j
public class QuoteRecoverHandler extends KafkaConsumerHandler<QuoteRecover> {
    private final MonitorService monitorService;

    public QuoteRecoverHandler(
            ObjectMapper objectMapper,
            @NotNull AppConf appConf,
            MonitorService monitorService
    ) {
        this.monitorService = monitorService;
        TypeReference<Message<QuoteRecover>> typeReference = new TypeReference<>() {
        };
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getQuoteRecover()),
                new Properties(), appConf.getMaxThread(appConf.getTopics().getQuoteRecover()));
    }

    public void handle(Message<QuoteRecover> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        SymbolQuote symbolQuote = message.getData();
        monitorService.rcv(symbolQuote);
    }

}
