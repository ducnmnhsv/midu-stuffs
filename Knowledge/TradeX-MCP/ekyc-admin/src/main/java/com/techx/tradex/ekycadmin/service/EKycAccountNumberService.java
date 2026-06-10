package com.techx.tradex.ekycadmin.service;

public interface EKycAccountNumberService {
    void updateAccountNumberInfo(Long eKycId, String ctxId);

    void updateAccountNumberInfoJob();
}
