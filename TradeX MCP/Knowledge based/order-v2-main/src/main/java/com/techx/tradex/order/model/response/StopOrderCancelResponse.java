package com.techx.tradex.order.model.response;

import lombok.Data;

@Data
public class StopOrderCancelResponse {
    private Boolean success;

    public StopOrderCancelResponse() {
        this(true);
    }

    public StopOrderCancelResponse(Boolean success) {
        this.success = success;
    }
}
