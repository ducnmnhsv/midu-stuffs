package com.techx.tradex.common.model.tuxedo;

import com.techx.tradex.common.model.requests.DataRequest;
import lombok.Data;

@Data
public class DrOrderHistoryRequest extends DataRequest {
    private String accountNumber;
    private String date;
    private String lastNextKey;
    private Integer fetchCount;
}
