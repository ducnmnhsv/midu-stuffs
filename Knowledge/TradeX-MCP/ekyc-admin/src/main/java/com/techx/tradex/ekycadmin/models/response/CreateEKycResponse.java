package com.techx.tradex.ekycadmin.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEKycResponse {
    @JsonProperty("eKycId")
    private String eKycId;
}
