package com.difisoft.marketcollector.model.lotte.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommonResponse {
    @JsonProperty("error_code")
    protected String errorCode;
    @JsonProperty("error_desc")
    protected String errorDesc;
    @JsonProperty("success")
    protected boolean success;
}
