package com.techx.tradex.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderMatchNotify {
    private String username;
    private String accountNumber;
    private String code;
    private String orderNumber;
    private String orderGroupNumber;
    private String status;
    private Double orderPrice;
    private Long orderQuantity;
    private Long matchQuantity;
    private String sellBuyType;
}
