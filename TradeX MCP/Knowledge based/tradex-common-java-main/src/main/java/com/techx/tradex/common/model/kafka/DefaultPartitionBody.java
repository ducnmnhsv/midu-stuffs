package com.techx.tradex.common.model.kafka;

public interface DefaultPartitionBody extends Body {
    default String getPartitionKey() {
        return null;
    }
    default String getMessageKey() {
        return this.getClass().getSimpleName();
    }
}
