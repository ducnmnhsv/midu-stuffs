package com.techx.tradex.ekycadmin.models.lotte;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LotteReq {

    @JsonProperty("lang_code")
    private String langCode;
}
