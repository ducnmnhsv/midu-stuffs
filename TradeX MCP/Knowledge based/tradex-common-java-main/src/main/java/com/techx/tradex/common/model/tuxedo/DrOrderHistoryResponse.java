package com.techx.tradex.common.model.tuxedo;

import lombok.Data;

@Data
public class DrOrderHistoryResponse {
    private String code;
    private String orderNumber;
    private String originalOrderNumber;
    private String accountNumber;
    private String modifyCancelType;
    private String sellBuyType;
    private Integer orderQuantity;
    private Double orderPrice;
    private String orderDate;
    private String orderTime;
    private String orderType;
    private Integer matchedQuantity;
    private Integer unmatchedQuantity;
    private String validity;
    private String rejectMessage;
    private OrderStatus orderStatus;
    private String nextKey;
}
