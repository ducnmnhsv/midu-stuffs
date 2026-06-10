package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.ConsumerHandler;
import com.difisoft.kafka.consumer.ThreadedKafkaConsumer;
import com.difisoft.market.model.v2.db.DealNotice;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.MonitorService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

public class DealNoticeUpdateHandler implements ConsumerHandler<String, String> {
    private static final Logger log = LoggerFactory.getLogger(DealNoticeUpdateHandler.class);

    private ObjectMapper objectMapper;
    private MonitorService monitorService;

    private TypeReference<Message<DealNotice>> typeReference = new TypeReference<Message<DealNotice>>() {
    };

    public DealNoticeUpdateHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            MonitorService monitorService
    ) {
        new ThreadedKafkaConsumer<>(appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getDealNoticeUpdate()),
                new Properties(), this, appConf.getMaxThread(appConf.getTopics().getDealNoticeUpdate()));
        this.objectMapper = objectMapper;
        this.monitorService = monitorService;
    }

    @Override
    public void handle(ConsumerRecord<String, String> consumerRecord) {
        Message<DealNotice> message;
        try {
            message = this.objectMapper.readValue(consumerRecord.value(), typeReference);
        } catch (IOException e) {
            log.error("fail to decode message {}", consumerRecord.value(), e);
            return;
        }
        this.handle(message);
    }

    protected void handle(Message<DealNotice> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        DealNotice dealNotice = message.getData();
        monitorService.rcv(dealNotice);
    }

}
