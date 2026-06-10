package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.domain.EContract;
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.models.request.EContractStatusReq;
import com.techx.tradex.ekycadmin.models.request.FptECSignRequest;
import com.techx.tradex.ekycadmin.models.request.LotteAccountNumberRequest;
import com.techx.tradex.ekycadmin.models.response.EContractStatusRes;
import com.techx.tradex.ekycadmin.models.response.FptECSignResponse;
import com.techx.tradex.ekycadmin.models.response.GenericResponse;
import com.techx.tradex.ekycadmin.models.response.LotteAccountNumberResponse;
import java.io.IOException;
import java.util.List;

public interface EContractCustomService {
    void initiateFptEContractJob();
    EContractStatusRes getEContractStatus(EContractStatusReq req);
    GenericResponse<FptECSignResponse> signEContract(FptECSignRequest request);
    Boolean initiateFptEContract(String prefixLog, EKyc ekyc);
    void jobExecuteInitiateFptEContract(String prefixLog, List<EKyc> eKycList, int index);
}
