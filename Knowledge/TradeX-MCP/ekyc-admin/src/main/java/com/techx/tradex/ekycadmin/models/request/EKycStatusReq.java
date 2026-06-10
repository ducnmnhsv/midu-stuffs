package com.techx.tradex.ekycadmin.models.request;

import com.techx.tradex.common.model.requests.DataRequest;
import lombok.Data;

@Data
public class EKycStatusReq extends DataRequest {

    private Object ids;
}
