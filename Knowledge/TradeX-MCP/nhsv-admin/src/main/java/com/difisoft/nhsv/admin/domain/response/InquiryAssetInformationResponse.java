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
public class InquiryAssetInformationResponse {
    @JsonProperty("error_desc")
    private String errorDesc;
    private boolean success;
    @JsonProperty("data_list")
    private List<InquiryAssetInformation> dataList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InquiryAssetInformation {
        @JsonProperty("total_asset")
        private Double totalAsset;
        @JsonProperty("cash_not_hold")
        private Double cashNotHold;
    }
}
