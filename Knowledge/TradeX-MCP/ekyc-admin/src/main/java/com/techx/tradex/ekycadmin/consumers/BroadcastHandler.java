package com.techx.tradex.ekycadmin.consumers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.kafka.KafkaConsumerHandler;
import com.techx.tradex.common.model.kafka.Message;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.service.TtlOpenAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Properties;

@Service
public class BroadcastHandler extends KafkaConsumerHandler<Object> {
    private static final Logger log = LoggerFactory.getLogger(BroadcastHandler.class);
    public static final String RELOAD_TTL_CITY_CODE_MAP = "/api/v1/ekyc-admin/reloadTtlCodeMap";

    private TtlOpenAccountService ttlOpenAccountService;

    @Autowired
    public BroadcastHandler(
        ObjectMapper objectMapper,
        AppConf appConf,
        TtlOpenAccountService ttlOpenAccountService
    ) {
        this.init(new TypeReference<Message<Object>>() {
                  }, objectMapper, appConf.getKafkaBootstraps(),
            appConf.getNodeId(), Arrays.asList(getBroadcastTopic(appConf)), new Properties(), 5);
        this.ttlOpenAccountService = ttlOpenAccountService;

    }

    @Override
    public void handle(Message<Object> message) {
        try {
            if (RELOAD_TTL_CITY_CODE_MAP.equals(message.getUri())) {
                this.ttlOpenAccountService.init();
            }
        } catch (Exception e) {
            log.error("Error on handling message: {}", message, e);
        }
    }

    public static String getBroadcastTopic(AppConf appConf) {
        return appConf.getClusterId() + ".broadcast";
    }
}
