package com.techx.tradex.common.model.kafka;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseDestination {
    private String topic;
    private String uri;

    public ResponseDestination(String topic, String uri) {
        this.topic = topic;
        this.uri = uri;
    }
}
