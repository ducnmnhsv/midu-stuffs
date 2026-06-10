package com.techx.tradex.order.services;


import com.difisoft.kafka.producer.KafkaRequestProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.Application;
import com.techx.tradex.order.configurations.AppConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestSender extends KafkaRequestProducer {
    public static final Logger log = LoggerFactory.getLogger(RequestSender.class);

    @Autowired
    public RequestSender(
            ObjectMapper objectMapper,
            AppConf appConf) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), Application.instanceId, true);
    }
}
