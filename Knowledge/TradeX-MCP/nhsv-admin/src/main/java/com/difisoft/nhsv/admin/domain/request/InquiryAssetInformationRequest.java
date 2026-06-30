package com.difisoft.nhsv.admin.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InquiryAssetInformationRequest {
    @JsonProperty("acnt_no")
    private String acntNO;
    @JsonProperty("sub_no")
    private String subNo;
    @JsonProperty("bank_code")
    private String bankCode;
}
