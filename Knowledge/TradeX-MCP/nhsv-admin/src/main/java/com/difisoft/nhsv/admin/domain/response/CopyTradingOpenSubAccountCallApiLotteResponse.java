package com.difisoft.nhsv.admin.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CopyTradingOpenSubAccountCallApiLotteResponse {
    @JsonProperty("error_desc")
    private String errorDesc;
    private boolean success;
}
