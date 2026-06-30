package com.difisoft.nhsv.admin.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketLeaderPeriodProfitLossResponse {
    private Long marketLeaderId;
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private ZonedDateTime currentDate;
    private Double profitLossRatio;
}
