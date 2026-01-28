package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.consumer.KafkaConsumerHandler;
import com.difisoft.market.model.v2.realtime.SymbolInfoUpdate;
import com.difisoft.model.kafka.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.InitService;
import com.techx.tradex.realtime.services.SymbolInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

public class SymbolInfoUpdateHandler extends KafkaConsumerHandler<SymbolInfoUpdate> {
    private static final Logger log = LoggerFactory.getLogger(SymbolInfoUpdateHandler.class);

    private final InitService initService;
    private final SymbolInfoService symbolInfoService;
    private final AppConf appConf;

    private TypeReference<Message<SymbolInfoUpdate>> typeReference = new TypeReference<>() {
    };

    public SymbolInfoUpdateHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            InitService initService,
            SymbolInfoService symbolInfoService
    ) {
        this.appConf = appConf;
        this.initService = initService;
        this.symbolInfoService = symbolInfoService;
        super.init(typeReference, objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(),
                Collections.singletonList(appConf.getTopics().getSymbolInfoUpdate()),
                new Properties(), appConf.getMaxThread(appConf.getTopics().getSymbolInfoUpdate()));
    }

    public void handle(Message<SymbolInfoUpdate> message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return;
        }
        try {
            SymbolInfoUpdate symbolInfoUpdate = message.getData();
            if (appConf.isEnableInitData()) {
                this.initService.handleSymbolInfoUpdate(symbolInfoUpdate);
            } else {
                symbolInfoService.updateBySymbolInfoUpdate(symbolInfoUpdate, true);
            }
        } catch (Exception e) {
            log.error("error while handle symbolInfoUpdate: {}", message.getMessageId(), e);
        }
    }

}
