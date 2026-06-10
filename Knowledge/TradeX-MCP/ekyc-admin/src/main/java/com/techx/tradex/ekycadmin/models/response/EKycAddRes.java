package com.techx.tradex.ekycadmin.models.response;

import com.techx.tradex.ekycadmin.domain.EKyc;

public class EKycAddRes {

    private Long eKycId;
    private String status;

    public Long geteKycId() {
        return eKycId;
    }

    public void seteKycId(Long eKycId) {
        this.eKycId = eKycId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static EKycAddRes fromEKyc(EKyc eKyc) {
        EKycAddRes res = new EKycAddRes();
        res.seteKycId(eKyc.getId());
        return res;
    }
}
