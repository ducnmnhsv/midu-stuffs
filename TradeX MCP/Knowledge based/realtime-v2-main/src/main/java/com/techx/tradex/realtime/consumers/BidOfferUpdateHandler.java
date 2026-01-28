package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.market.model.v2.db.BidOffer;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Properties;

public class BidOfferUpdateHandler extends KafkaConsumerHandler<BidOffer> {
    private static final Logger log = LoggerFactory.getLogger(BidOfferUpdateHandler.class);

    private final MonitorService monitorService;

    public BidOfferUpdateHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            MonitorService monitorService
    ) {
        this.monitorService = monitorService;
        TypeReference<Message<BidOffer>> typeReference = new TypeReference<Message<BidOffer>>() {
        };
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getBidOfferUpdate()), new Properties(),
                appConf.getMaxThread(appConf.getTopics().getBidOfferUpdate()));
    }

    public void handle(Message<BidOffer> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        BidOffer bidOffer = message.getData();
        monitorService.rcv(bidOffer);
    }
}
