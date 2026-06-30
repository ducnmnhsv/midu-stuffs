package com.difisoft.nhsv.admin.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CopyTradingOpenSubAccountCallApiLotteRequest {
    @JsonProperty("acnt_no")
    private String acntNo;
    @JsonProperty("sub_no")
    private String subNo;
    @JsonProperty("fee_bk")
    private String feeBk;
    private String idno;
}
