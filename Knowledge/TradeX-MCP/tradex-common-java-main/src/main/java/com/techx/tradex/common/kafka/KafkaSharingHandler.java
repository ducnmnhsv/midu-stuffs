package com.techx.tradex.common.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.model.kafka.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * this class supports to consume multiple topics using one consumer only,
 * It also support  to mapper to exactly class if possible.
 */
public class KafkaSharingHandler extends KafkaSharingConsumer<String, String> {
    private static final Logger log = LoggerFactory.getLogger(KafkaSharingHandler.class);

    private ObjectMapper objectMapper;
    private Map<String, Handler> topics;

    public KafkaSharingHandler(ObjectMapper objectMapper, String bootStrapServer, String groupId, Properties properties, Map<String, Consumer<Message>> topics, int maxThread) {
        super(bootStrapServer, groupId, properties, null, maxThread);
        this.topics = new HashMap<>();
        this.objectMapper = objectMapper;
    }

    public <T> void addTopic(String topic, Handler<T> handler) {
        this.topics.put(topic, handler);
        super.addTopic(topic, null);
    }

    protected void createHandler() {
        this.handler = record -> {
            String topic = record.topic();
            String msgString = record.value();
            Handler handler = this.topics.get(topic);
            if (handler == null) {
                log.error("cannot handle msg {} from topic {}", msgString, topic);
                return;
            }
            final Message msg;
            try {
                msg = objectMapper.readValue(msgString, handler.getTypeRef());
            } catch (Exception e) {
                log.error("fail to decode message {}, {}", msgString, e);
                return;
            }
            handler.handle(msg);
        };
    }

    public interface Handler<T> {
        void handle(Message<T> message);

        TypeReference<Message<T>> getTypeRef();
    }
}
