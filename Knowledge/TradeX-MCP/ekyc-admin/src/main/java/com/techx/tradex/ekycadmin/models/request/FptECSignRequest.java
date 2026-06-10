package com.techx.tradex.ekycadmin.models.request;

import com.difisoft.model.requests.DataRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FptECSignRequest extends DataRequest {

    private String identifierId;

    @JsonProperty(value = "eKycId")
    private String eKycId;
}
