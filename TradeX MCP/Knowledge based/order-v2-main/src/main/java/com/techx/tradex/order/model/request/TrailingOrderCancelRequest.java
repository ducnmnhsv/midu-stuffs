package com.techx.tradex.order.model.request;

import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.validator.CombineValidator;
import com.difisoft.model.utils.validator.NumberValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrailingOrderCancelRequest extends DataRequest {
    private Long trailingOrderId;

    public void validate() {
        new CombineValidator()
                .add(new NumberValidator<>("trailingOrderId", this.trailingOrderId))
                .check();
    }
}
