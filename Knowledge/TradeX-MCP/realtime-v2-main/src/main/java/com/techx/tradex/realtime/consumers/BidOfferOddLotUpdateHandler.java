package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.market.model.v2.db.BidOfferOddLot;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

public class BidOfferOddLotUpdateHandler extends KafkaConsumerHandler<BidOfferOddLot> {
    private static final Logger log = LoggerFactory.getLogger(BidOfferOddLotUpdateHandler.class);

    private final MonitorService monitorService;

    public BidOfferOddLotUpdateHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            MonitorService monitorService
    ) {
        this.monitorService = monitorService;
        TypeReference<Message<BidOfferOddLot>> typeReference = new TypeReference<Message<BidOfferOddLot>>() {
        };
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getBidOfferOddLotUpdate()),
                new Properties(), appConf.getMaxThread(appConf.getTopics().getBidOfferOddLotUpdate()));
    }

    public void handle(Message<BidOfferOddLot> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        BidOfferOddLot bidOfferOddLot = message.getData();
        monitorService.rcv(bidOfferOddLot);
    }
}
