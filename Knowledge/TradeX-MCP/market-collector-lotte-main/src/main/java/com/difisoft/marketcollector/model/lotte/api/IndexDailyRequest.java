package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndexDailyRequest {
    @JsonProperty("idx")
    private String indexRefCode;
    @JsonProperty("dt")
    private String toDate;
    @JsonProperty("next_data")
    private String nextData;
    @JsonProperty("row_count")
    private Integer rowCount;
}
