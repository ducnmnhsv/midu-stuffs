package com.techx.tradex.ekycadmin.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.kafka.KafkaRequestSender;
import com.techx.tradex.ekycadmin.config.AppConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RequestSender extends KafkaRequestSender {
    public static final Logger log = LoggerFactory.getLogger(RequestSender.class);

    @Autowired
    public RequestSender(
        ObjectMapper objectMapper,
        AppConf appConf) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), UUID.randomUUID().toString());
        this.setDefaultTimeout(15000);
    }
}
