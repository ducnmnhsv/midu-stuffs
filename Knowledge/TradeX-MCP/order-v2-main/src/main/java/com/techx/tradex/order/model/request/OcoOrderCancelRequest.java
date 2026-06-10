package com.techx.tradex.order.model.request;

import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.validator.CombineValidator;
import com.difisoft.model.utils.validator.NumberValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OcoOrderCancelRequest extends DataRequest {
    private Long ocoOrderId;

    public void validate() {
        new CombineValidator()
                .add(new NumberValidator<>("ocoOrderId", this.ocoOrderId))
                .check();
    }

}
