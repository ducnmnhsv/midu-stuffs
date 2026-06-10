package com.techx.tradex.ekycadmin.models.request;

import lombok.Data;

@Data
public class EContractStatusReq {
    private String data;
    private String refId;
    private String envelopeId;
    private String contactId;
    private String contractStatus;
    private String contactIdAction;
}
