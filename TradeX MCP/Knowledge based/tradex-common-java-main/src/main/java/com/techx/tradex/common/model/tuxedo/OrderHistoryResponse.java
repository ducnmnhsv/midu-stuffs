package com.techx.tradex.common.model.tuxedo;

import lombok.Data;

@Data
public class OrderHistoryResponse {
    private String accountNumber;
    private String subNumber;
    private String stockCode;
    private String orderDate;
    private String orderTime;
    private String sellBuyType;
    private String orderType;
    private Integer orderQuantity;
    private Double orderPrice;
    private Double orderAmount;
    private Integer matchedQuantity;
    private Double matchedPrice;
    private Double matchedAmount;
    private Integer unmatchedQuantity;
    private String modifyCancelType;
    private Integer modifyCancelQuantity;
    private OrderStatus orderStatus;
    private String orderNumber;
    private String originalOrderNumber;
    private String username;
    private String branchCode;

    public String getNextKey() {
        return String.format("%s-%s-%s", this.orderDate ,this.branchCode ,this.matchedPrice);
    }
}
