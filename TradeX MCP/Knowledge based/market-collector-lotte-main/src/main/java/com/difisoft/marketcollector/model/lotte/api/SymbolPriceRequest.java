package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class SymbolPriceRequest {
    @JsonProperty("stk_cds")
    private List<String> symbols = new ArrayList<>();
    @JsonProperty("bo_cnt")
    private String boCnt = "5";
}
