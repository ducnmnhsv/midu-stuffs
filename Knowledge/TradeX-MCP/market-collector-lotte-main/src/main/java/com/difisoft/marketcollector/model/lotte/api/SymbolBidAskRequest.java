package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SymbolBidAskRequest {
    @JsonProperty("stk_cd")
    private String symbol;
    @JsonProperty("bo_cnt")
    private String boCnt = "10";

    public SymbolBidAskRequest(String symbol) {
        this.symbol = symbol;
    }
}
