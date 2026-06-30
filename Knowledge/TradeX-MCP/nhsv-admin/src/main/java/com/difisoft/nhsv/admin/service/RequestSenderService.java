package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.producer.KafkaRequestProducer;
import com.difisoft.nhsv.admin.config.ApplicationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.UUID;

@Service("requestSenderService")
@Slf4j
public class RequestSenderService extends KafkaRequestProducer {

    @Autowired
    public RequestSenderService(ObjectMapper objectMapper, ApplicationProperties appConf) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), UUID.randomUUID().toString(), true, new Properties(), true);
        this.setDefaultTimeout(15000);
    }
}
