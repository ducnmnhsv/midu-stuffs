package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SymbolNameRequest {
    @JsonProperty("key_search")
    private String search = ""; // "" for all
    @JsonProperty("mkt_tp")
    private String exchange = "%"; // % for all
    @JsonProperty("stk_tp")
    private String symbolType = "%"; // % for all
    @JsonProperty("next_data")
    private String nextData; // % for all

    public SymbolNameRequest(String nextData) {
        this.nextData = nextData;
    }
}
