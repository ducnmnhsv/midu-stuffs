package com.difisoft.nhsv.admin.domain.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketLeaderSubGrowthRateResponse {
    @JsonProperty
    private String beMarketLeaderDate;

    private List<MarketLeaderSubGrowthRateItem> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MarketLeaderSubGrowthRateItem {
        @JsonProperty
        private String date;

        private Long totalSubscribers;
    }
}
