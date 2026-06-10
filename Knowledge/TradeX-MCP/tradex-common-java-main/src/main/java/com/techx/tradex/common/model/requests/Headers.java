package com.techx.tradex.common.model.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Headers {
    protected Token token;
    protected Token secToken;
    @JsonProperty("accept-language")
    protected String acceptLanguage;
    protected String platform;
}
