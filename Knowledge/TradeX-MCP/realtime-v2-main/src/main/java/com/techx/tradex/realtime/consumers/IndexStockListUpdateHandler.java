package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.market.model.v2.db.IndexStockList;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Properties;

public class IndexStockListUpdateHandler extends KafkaConsumerHandler<IndexStockList> {
    private static final Logger log = LoggerFactory.getLogger(IndexStockListUpdateHandler.class);

    private final MonitorService monitorService;

    @Autowired
    public IndexStockListUpdateHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            MonitorService monitorService) {
        this.monitorService = monitorService;
        TypeReference<Message<IndexStockList>> typeReference = new TypeReference<Message<IndexStockList>>() {
        };
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getIndexStockListUpdate()),
                new Properties(), appConf.getMaxThread(appConf.getTopics().getIndexStockListUpdate()));
    }

    public void handle(Message<IndexStockList> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        IndexStockList indexStockList = message.getData();
        monitorService.rcv(indexStockList);
    }
}
