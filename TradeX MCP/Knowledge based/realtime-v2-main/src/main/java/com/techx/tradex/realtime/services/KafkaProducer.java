package com.techx.tradex.realtime.services;


import com.difisoft.kafka.producer.KafkaRequestProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.Application;
import com.techx.tradex.realtime.configurations.AppConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class KafkaProducer extends KafkaRequestProducer {
    public static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    public KafkaProducer(
            ObjectMapper objectMapper,
            AppConf appConf) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), Application.instanceId, true, new Properties(), true);
        this.setDefaultTimeout(15000);
    }
}
