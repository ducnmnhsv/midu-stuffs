package com.techx.tradex.ekycadmin.models.dto;

import com.techx.tradex.ekycadmin.domain.EContract;

public interface IEContractInfo {
    Long getId();
    String getContractStatus();
    String getCustomerSignatueStatus();
    String getSecuritiesSignatureStatus();
    String getTemplateId();
    String getRequestData();
    String getSignFileContent();
    String getContractFileContent();
    EContract getEcontract();
    String getContractFileName();
    void setContractFileName(String contractFileName);
}
