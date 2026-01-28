package com.techx.tradex.common.model.kafka.response.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MarketIndexResponse implements Serializable {
    private String code;
    private String market;
    private String indexName;
    private String indexNameEn;
    private boolean isHighlight;

    @JsonProperty("isHighlight")
    public boolean isHighlight() {
        return isHighlight;
    }
}

