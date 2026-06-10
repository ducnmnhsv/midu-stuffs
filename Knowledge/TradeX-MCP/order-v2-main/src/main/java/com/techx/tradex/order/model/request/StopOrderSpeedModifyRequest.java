package com.techx.tradex.order.model.request;

import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.validator.CombineValidator;
import com.difisoft.model.utils.validator.EnumValidator;
import com.difisoft.model.utils.validator.NumberValidator;
import com.difisoft.model.utils.validator.StringValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopOrderSpeedModifyRequest extends DataRequest {
    private String accountNumber;
    private String subNumber;
    private String code;
    private String sellBuyType;
    private Double stopPrice;
    private Double newStopPrice;

    public void validate() {
        new CombineValidator()
                .add(new StringValidator("accountNumber", this.accountNumber).empty())
                .add(new StringValidator("code", this.code).empty())
                .add(new StringValidator("sellBuyType", this.sellBuyType).empty())
                .add(new EnumValidator<>("sellBuyType", this.sellBuyType, SellBuyTypeEnum.class))
                .add(new NumberValidator("stopPrice", this.stopPrice))
                .add(new NumberValidator("newStopPrice", this.newStopPrice))
                .check();
    }

    public SellBuyTypeEnum getSellBuyType() {
        return this.sellBuyType == null ? null : SellBuyTypeEnum.valueOf(this.sellBuyType);
    }
}
