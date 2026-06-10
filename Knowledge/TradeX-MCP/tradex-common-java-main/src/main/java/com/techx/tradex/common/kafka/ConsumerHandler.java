package com.techx.tradex.common.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface ConsumerHandler<K, V> {
    void handle(ConsumerRecord<K, V> record);
}
