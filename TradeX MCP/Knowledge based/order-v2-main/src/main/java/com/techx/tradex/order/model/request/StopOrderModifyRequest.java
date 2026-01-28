package com.techx.tradex.order.model.request;

import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.validator.CombineValidator;
import com.difisoft.model.utils.validator.NumberValidator;
import com.difisoft.model.utils.validator.StringValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopOrderModifyRequest extends DataRequest {
    private Long stopOrderId;
    private Long orderQuantity;
    private Double stopPrice;
    private Double orderPrice;
    private String fromDate;
    private String toDate;


    public void validate() {
        if (this.stopPrice == null) {
            this.stopPrice = 0d;
        }
        CombineValidator validator = new CombineValidator();
        if (this.orderPrice != null && this.orderPrice > 0) {
            validator.add(new NumberValidator<>("orderPrice", this.orderPrice));
        }
        validator.add(new NumberValidator<>("stopOrderId", this.stopOrderId))
                .add(new NumberValidator<>("orderQuantity", this.orderQuantity))
                .add(new NumberValidator<>("stopPrice", this.stopPrice))
                .add(new StringValidator("fromDate", this.fromDate).empty())
                .add(new StringValidator("toDate", this.toDate).empty())
                .check();
    }

    @Override
    public String getUsername() {
        return this.getHeaders().getToken().getUserData().getUsername();
    }
}
