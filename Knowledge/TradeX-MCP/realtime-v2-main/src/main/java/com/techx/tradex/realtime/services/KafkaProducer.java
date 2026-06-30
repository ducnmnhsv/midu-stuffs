package com.techx.tradex.realtime.services;

import com.difisoft.kafka.producer.KafkaRequestProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.Application;
import com.techx.tradex.realtime.configurations.AppConf;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Future;

@Service
public class KafkaProducer extends KafkaRequestProducer {
    public static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final com.difisoft.kafka.producer.KafkaProducer<String, String> rawProducer;

    @Autowired
    public KafkaProducer(
            ObjectMapper objectMapper,
            AppConf appConf) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), Application.instanceId, true, new Properties(), true);
        this.rawProducer = new com.difisoft.kafka.producer.KafkaProducer<>(
                appConf.getKafkaBootstraps(),
                Application.instanceId + ".raw",
                new Properties(),
                true);
        this.setDefaultTimeout(15000);
    }

    public Future<RecordMetadata> sendRawMessage(String topic, String key, String value, Map<String, String> headers) {
        List<Header> kafkaHeaders = new ArrayList<>();
        if (Objects.nonNull(headers)) {
            headers.forEach((headerKey, headerValue) -> {
                if (Objects.nonNull(headerKey) && Objects.nonNull(headerValue)) {
                    kafkaHeaders.add(new RecordHeader(headerKey, headerValue.getBytes(StandardCharsets.UTF_8)));
                }
            });
        }
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, key, value, kafkaHeaders);
        return rawProducer.send(record);
    }
}
