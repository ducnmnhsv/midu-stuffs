package com.techx.tradex.order.model.request;

import com.difisoft.model.requests.DataRequest;
import lombok.Data;

@Data
public class BullBearOrderDetailRequest extends DataRequest {
    private long id;
}
