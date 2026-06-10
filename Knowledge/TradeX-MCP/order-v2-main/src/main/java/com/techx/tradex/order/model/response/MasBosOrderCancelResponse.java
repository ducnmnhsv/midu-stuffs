package com.techx.tradex.order.model.response;

import lombok.Data;

@Data
public class MasBosOrderCancelResponse {
    private String orderNo;
    private String orderGroupNo;
    private Boolean success;
    private String rejectCause;
}
