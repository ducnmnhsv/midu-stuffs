package com.techx.tradex.common.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.handler.BaseRequestHandler;
import com.techx.tradex.common.model.kafka.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *  this class is use for consuming message without response
 */

public abstract class KafkaConsumerHandler<T> {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerHandler.class);

    protected ThreadedKafkaConsumer<String, String> consumer;

    protected void init(TypeReference<Message<T>> typeReference, ObjectMapper om, String bootStrapServer, String groupId, List<String> topics, Properties consumerProperties, int maxThread) {
        if (consumer != null) {
            throw new IllegalStateException("Consumer has been init. you should create a new instance if the consumer has stopped: " +
                    consumer.isMarkAsStop() + ":" + consumer.isRealStop());
        }
        this.consumer = new ThreadedKafkaConsumer<>(
                bootStrapServer,
                groupId,
                topics,
                consumerProperties == null ? new Properties() : consumerProperties,
                record -> {
                    String value = record.value();
                    String key = record.key();
                    log.info("receive msg: {} with key {}", value, key);
                    Message<T> msg;
                    try {
                        msg = om.readValue(value, typeReference);
                    } catch (Exception e) {
                        log.error("fail to parse message {} {}", value, key, e);
                        return;
                    }
                    if (msg != null) {
                        try {
                            this.handle(msg);
                        } catch (Exception e) {
                            log.error("fail to handle message {}-{}", msg.getMessageId(), msg.getTransactionId(), e);
                        }
                    }
                },
                maxThread
        );
    }

    public abstract void handle(Message<T> message);

    public void stop() {
        if (this.consumer != null) {
            this.consumer.stop();
        }
    }
}
