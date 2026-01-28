package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IndexListRequest {
    @JsonProperty("mkt_tp")
    private String exchange = "%";
    @JsonProperty("key_search")
    private String search = "";
    @JsonProperty("next_data")
    private String nextData;

    public IndexListRequest(String nextData) {
        this.nextData = nextData;
    }
}
