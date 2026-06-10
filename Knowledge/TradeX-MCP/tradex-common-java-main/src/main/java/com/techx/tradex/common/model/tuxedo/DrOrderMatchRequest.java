package com.techx.tradex.common.model.tuxedo;

import com.techx.tradex.common.model.requests.DataRequest;
import lombok.Data;

@Data
public class DrOrderMatchRequest extends DataRequest {
    private String accountNumber;
    private String password; // no need to pass
    private String favoriteListGroupName; // no need to pass
    private String lastNextKey;
    private Integer fetchCount;
}
