package com.difisoft.nhsv.admin.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketLeaderProfitLossResponse {

    @JsonProperty
    public String beMarketLeaderDate;
    @JsonProperty
    public Long marketLeaderId;
    @JsonProperty
    public List<MarketLeaderDailyProfitLossItem> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MarketLeaderDailyProfitLossItem {
        @JsonProperty
        public String reportDate;
        @JsonProperty
        public Double profitLossRatio;
        @JsonProperty
        public Double normalisedNAV;
    }
}
