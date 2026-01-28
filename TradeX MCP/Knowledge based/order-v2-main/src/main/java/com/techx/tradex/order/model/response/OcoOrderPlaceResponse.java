package com.techx.tradex.order.model.response;

import com.techx.tradex.order.model.db.OcoOrder;
import lombok.Data;

@Data
public class OcoOrderPlaceResponse {
    private Long id;

    public static OcoOrderPlaceResponse fromOcoOrder(OcoOrder ocoOrder) {
        OcoOrderPlaceResponse response = new OcoOrderPlaceResponse();
        response.setId(ocoOrder.getId());
        return response;
    }
}
