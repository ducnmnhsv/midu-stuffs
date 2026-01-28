package com.techx.tradex.common.model.tuxedo;

import lombok.Data;

@Data
public class OrderStatusUpdate {
    private String orderNumber;
    private String originalOrderNumber;
    private double orderPrice;
    private int orderQuantity;
    private String accountNumber;
    private String sellBuyType;
    private String marketType;
    private String time;
    private String execNo;
    private double matchPrice;
    private int matchQuantity;
    private int totalMatchQuantity;
    private int unmatchQuantity;
    private int modifyCancelQuantity;
    private String username;
    private String code;
    private String subNumber;
    private String status;
}
