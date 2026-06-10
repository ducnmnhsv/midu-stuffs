package com.techx.tradex.ekycadmin.models.lotte;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LotteRes {

    @JsonProperty("error_code")
    String errorCode;

    @JsonProperty("error_desc")
    String errorDesc;

    Boolean success;

    @JsonProperty("total_record")
    String totalRecord;
}
