package com.techx.tradex.order.model.response;

import lombok.Data;

@Data
public class StopOrderModifyResponse {
    private Boolean success;

    public StopOrderModifyResponse() {
        this(true);
    }

    public StopOrderModifyResponse(Boolean success) {
        this.success = success;
    }
}
