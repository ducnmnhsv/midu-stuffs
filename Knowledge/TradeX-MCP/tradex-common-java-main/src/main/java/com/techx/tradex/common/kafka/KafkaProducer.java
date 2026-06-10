package com.techx.tradex.common.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaProducer<K, V> {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    private org.apache.kafka.clients.producer.KafkaProducer<K, V> producer;

    public KafkaProducer(String bootStrapServer, String clientId, Properties properties) {
        this.init(bootStrapServer, clientId, properties, false);
    }

    public KafkaProducer(String bootStrapServer, String clientId, Properties properties, boolean enableIdempotence) {
        this.init(bootStrapServer, clientId, properties, enableIdempotence);
    }

    private void init(String bootStrapServer, String clientId, Properties properties, boolean enableIdempotence) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId != null ? clientId : "" + System.currentTimeMillis());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        props.putAll(properties);
        if (enableIdempotence) {
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
            props.put(ProducerConfig.ACKS_CONFIG, "-1");
        }
        log.warn("init producer {}", bootStrapServer);
        this.producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
    }

    public Future<RecordMetadata> send(ProducerRecord<K, V> record) {
        return this.producer.send(record);
    }
}
