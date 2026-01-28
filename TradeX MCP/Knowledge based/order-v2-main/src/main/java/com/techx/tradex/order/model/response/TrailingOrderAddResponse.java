package com.techx.tradex.order.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techx.tradex.order.model.db.TrailingOrder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrailingOrderAddResponse {
    private long trailingOrderId;

    public static TrailingOrderAddResponse fromTrailingOrder(TrailingOrder trailingOrder) {
        TrailingOrderAddResponse response = new TrailingOrderAddResponse();
        response.setTrailingOrderId(trailingOrder.getId());
        return response;
    }
}
