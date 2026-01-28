package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.market.model.v2.db.Advertised;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.MonitorService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Properties;

@Slf4j
public class AdvertisedUpdateHandler extends KafkaConsumerHandler<Advertised> {

    private MonitorService monitorService;

    public AdvertisedUpdateHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            MonitorService monitorService
    ) {
        this.monitorService = monitorService;
        super.init(
                new TypeReference<Message<Advertised>>() {
                }, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), Collections.singletonList(appConf.getTopics().getAdvertisedUpdate()),
                new Properties(), appConf.getMaxThread(appConf.getTopics().getAdvertisedUpdate()));
    }

    public void handle(Message<Advertised> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        Advertised advertised = message.getData();
        monitorService.rcv(advertised);
    }

}
