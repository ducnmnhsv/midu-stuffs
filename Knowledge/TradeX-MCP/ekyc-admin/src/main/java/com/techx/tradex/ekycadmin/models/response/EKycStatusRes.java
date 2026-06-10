package com.techx.tradex.ekycadmin.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EKycStatusRes {
    private Long id;
    private String identifierId;
    private String phoneNumber;
    private String fullName;
    private String status;
    private String creatorStatus;
    private String creatorReason;
    private String creatorFullResult;
}
