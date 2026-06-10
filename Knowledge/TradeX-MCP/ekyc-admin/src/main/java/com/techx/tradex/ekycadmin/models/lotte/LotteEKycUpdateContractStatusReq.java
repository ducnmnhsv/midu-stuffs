package com.techx.tradex.ekycadmin.models.lotte;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LotteEKycUpdateContractStatusReq  {
    @JsonProperty("acnt_no")
    private String accountNumber;
}