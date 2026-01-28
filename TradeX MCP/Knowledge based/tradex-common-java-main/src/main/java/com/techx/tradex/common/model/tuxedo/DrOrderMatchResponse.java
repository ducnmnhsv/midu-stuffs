package com.techx.tradex.common.model.tuxedo;

import lombok.Data;

@Data
public class DrOrderMatchResponse {
    private String accountNumber;
    private String accountName;
    private String orderNumber;
    private String code;
    private String sellBuyType;
    private Double matchPrice;
    private Integer matchedQuantity;
    private Integer unmatchedQuantity;
    private String time;
    private Integer liquidationPositionNumber;
    private String nextKey;
}
