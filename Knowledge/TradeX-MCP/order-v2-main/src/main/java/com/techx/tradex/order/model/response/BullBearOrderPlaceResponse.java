package com.techx.tradex.order.model.response;

import com.techx.tradex.order.model.db.BullBearOrder;
import lombok.Data;

@Data
public class BullBearOrderPlaceResponse {
    private Long id;

    public static BullBearOrderPlaceResponse fromBullBearOrder(BullBearOrder bullBearOrder) {
        BullBearOrderPlaceResponse response = new BullBearOrderPlaceResponse();
        response.setId(bullBearOrder.getId());
        return response;
    }
}
