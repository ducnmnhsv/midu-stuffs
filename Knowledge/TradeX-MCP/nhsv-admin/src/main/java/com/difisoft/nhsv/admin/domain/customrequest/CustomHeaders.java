package com.difisoft.nhsv.admin.domain.customrequest;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(
    ignoreUnknown = true
)
public class CustomHeaders {
    protected CustomToken token;
    protected CustomToken secToken;
    @JsonProperty("accept-language")
    protected String acceptLanguage;
    protected String platform;
    protected String rid;
    protected Map<String, Object> otherHeaders = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> any() {
        return this.otherHeaders;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        this.otherHeaders.put(name, value);
    }
}
