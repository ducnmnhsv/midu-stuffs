package com.techx.tradex.order.model.response;

import lombok.Data;

@Data
public class MasBosOrderPlaceResponse {
    private String orderNumber;
    private String orderGroupID;
    private String success;
    private String message;
}
