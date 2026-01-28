package com.techx.tradex.common.model.kafka;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ArrayBody extends ArrayList implements Body {
    private String partitionKey;
    private String messageKey;

    public ArrayBody(String partitionKey, String messageKey) {
        this.partitionKey = partitionKey;
        this.messageKey = messageKey;
    }
}
