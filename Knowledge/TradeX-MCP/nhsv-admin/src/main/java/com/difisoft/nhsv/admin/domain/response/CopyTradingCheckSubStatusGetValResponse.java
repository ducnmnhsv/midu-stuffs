package com.difisoft.nhsv.admin.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopyTradingCheckSubStatusGetValResponse {
    @JsonProperty("error_desc")
    private String errorDesc;
    private boolean success;
    @JsonProperty("data_list")
    private List<Val> dataList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Val {
        private String val;
    }
}
