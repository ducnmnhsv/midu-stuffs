package com.techx.tradex.realtime.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EodBackfillRequest {

    @JsonProperty("from_date")
    private String fromDate;

    @JsonProperty("to_date")
    private String toDate;
}
