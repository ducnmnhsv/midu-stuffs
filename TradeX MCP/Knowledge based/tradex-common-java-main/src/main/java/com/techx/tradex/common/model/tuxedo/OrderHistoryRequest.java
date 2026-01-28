package com.techx.tradex.common.model.tuxedo;

import com.techx.tradex.common.constants.MatchType;
import com.techx.tradex.common.model.requests.DataRequest;
import lombok.Data;

@Data
public class OrderHistoryRequest extends DataRequest {
    private String accountNumber;
    private String subNumber;
    private String fromDate;
    private String toDate;
    private String stockCode;
    private String sellBuyType;
    private MatchType matchType;
    private String sortType;
    private String lastOrderDate;
    private String lastBranchCode;
    private String lastOrderNumber;
    private Double lastMatchPrice;
    private Integer fetchCount;
    private String marketType;
}
