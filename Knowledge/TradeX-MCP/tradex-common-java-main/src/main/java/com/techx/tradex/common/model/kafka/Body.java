package com.techx.tradex.common.model.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Body {
    @JsonIgnore
    String getPartitionKey();
    @JsonIgnore
    String getMessageKey();
}
