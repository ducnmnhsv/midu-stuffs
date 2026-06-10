package com.techx.tradex.common.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class KafkaConsumer<K, V> implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    protected Consumer<K, V> consumer;
    protected ConsumerHandler<K, V> handler;
    private boolean markAsStop = false;
    private boolean realStop = false;
    protected String key;

    protected KafkaConsumer() {

    }

    public KafkaConsumer(String bootStrapServer, String groupId, Collection<String> topics, Properties properties, ConsumerHandler<K, V> handler) {
        this.init(bootStrapServer, groupId, topics, properties, handler);
    }

    protected void init(String bootStrapServer, String groupId, Collection<String> topics, Properties properties, ConsumerHandler<K, V> handler) {
        this.handler = handler;
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");//latest, earliest, none
        this.key = bootStrapServer + "::" + topics.toString();
        log.warn("init consume kafka {} _ group {}", this.key, groupId);
        props.putAll(properties);
        consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props);
        consumer.subscribe(topics);
    }

    @Override
    public void run() {
        log.warn("start consume kafka {}", this.key);
        while (true) {
            if (this.markAsStop) {
                this.realStop = true;
                break;
            }
            ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<K, V> record : records) {
                handler.handle(record);
            }
        }
    }

    public void stop() {
        this.markAsStop = true;
    }

    public boolean isMarkAsStop() {
        return markAsStop;
    }

    public boolean isRealStop() {
        return realStop;
    }
}
