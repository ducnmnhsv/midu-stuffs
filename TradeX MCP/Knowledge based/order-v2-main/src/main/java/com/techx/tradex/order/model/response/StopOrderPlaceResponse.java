package com.techx.tradex.order.model.response;

import com.techx.tradex.order.model.db.StopOrder;
import lombok.Data;

@Data
public class StopOrderPlaceResponse {
    private Long id;

    public static StopOrderPlaceResponse fromStopOrder(StopOrder stopOrder) {
        StopOrderPlaceResponse response = new StopOrderPlaceResponse();
        response.setId(stopOrder.getId());
        return response;
    }
}
