package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.market.model.v2.db.SymbolQuote;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Properties;

@Slf4j
public class QuoteUpdateHandler extends KafkaConsumerHandler<SymbolQuote> {

    private final MonitorService monitorService;

    public QuoteUpdateHandler(
            ObjectMapper objectMapper,
            @NotNull AppConf appConf,
            MonitorService monitorService
    ) {
        this.monitorService = monitorService;
        TypeReference<Message<SymbolQuote>> typeReference = new TypeReference<>() {
        };
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getQuoteUpdate()),
                new Properties(), appConf.getMaxThread(appConf.getTopics().getQuoteUpdate()));
    }

    public void handle(Message<SymbolQuote> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        SymbolQuote symbolQuote = message.getData();
        monitorService.rcv(symbolQuote);
    }

}
