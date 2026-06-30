package com.techx.tradex.notification.services;

import com.techx.tradex.common.kafka.KafkaRequestSender;
import org.springframework.stereotype.Service;

@Service
public class RequestSender {
    private KafkaRequestSender requestSender;

    public KafkaRequestSender get() {
        return requestSender;
    }

    public void setRequestSender(KafkaRequestSender requestSender) {
        this.requestSender = requestSender;
    }
}
