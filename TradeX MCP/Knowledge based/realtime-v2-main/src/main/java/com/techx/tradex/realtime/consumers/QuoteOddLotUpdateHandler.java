package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.market.model.v2.db.SymbolQuoteOddLot;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.MonitorService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Properties;

@Slf4j
public class QuoteOddLotUpdateHandler extends KafkaConsumerHandler<SymbolQuoteOddLot> {

    private final MonitorService monitorService;

    public QuoteOddLotUpdateHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            MonitorService monitorService
    ) {
        this.monitorService = monitorService;
        TypeReference<Message<SymbolQuoteOddLot>> typeReference = new TypeReference<Message<SymbolQuoteOddLot>>() {
        };
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getQuoteOddLotUpdate()),
                new Properties(), appConf.getMaxThread(appConf.getTopics().getQuoteOddLotUpdate()));
    }

    public void handle(Message<SymbolQuoteOddLot> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        SymbolQuoteOddLot symbolQuote = message.getData();
        monitorService.rcv(symbolQuote);
//        this.requestHandler.sendMiniMessageSafeNoResponse(appConf.getTopics().getQuoteMonitor(), "Update", symbolQuote);
    }

}
