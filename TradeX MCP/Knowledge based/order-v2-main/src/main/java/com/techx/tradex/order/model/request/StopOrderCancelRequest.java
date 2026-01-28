package com.techx.tradex.order.model.request;

import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.validator.CombineValidator;
import com.difisoft.model.utils.validator.NumberValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopOrderCancelRequest extends DataRequest {
    private Long stopOrderId;

    public void validate() {
        new CombineValidator()
                .add(new NumberValidator<>("stopOrderId", this.stopOrderId))
                .check();
    }
}
