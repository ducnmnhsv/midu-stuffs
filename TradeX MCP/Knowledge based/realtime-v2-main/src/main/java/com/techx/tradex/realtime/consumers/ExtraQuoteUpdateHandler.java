package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.model.request.ExtraQuote;
import com.techx.tradex.realtime.services.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

public class ExtraQuoteUpdateHandler extends KafkaConsumerHandler<ExtraQuote> {
    private static final Logger log = LoggerFactory.getLogger(ExtraQuoteUpdateHandler.class);

    private final MonitorService monitorService;

    public ExtraQuoteUpdateHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            MonitorService monitorService
    ) {
        this.monitorService = monitorService;
        TypeReference<Message<ExtraQuote>> typeReference = new TypeReference<Message<ExtraQuote>>() {
        };
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getExtraUpdate()),
                new Properties(), appConf.getMaxThread(appConf.getTopics().getExtraUpdate()));
    }

    public void handle(Message<ExtraQuote> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        ExtraQuote extraQuote = message.getData();
        monitorService.rcv(extraQuote);
    }
}
