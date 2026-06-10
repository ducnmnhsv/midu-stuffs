package com.techx.tradex.order.model.request;

import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.validator.CombineValidator;
import com.difisoft.model.utils.validator.StringValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopOrderSpeedCancelRequest extends DataRequest {
    private String accountNumber;
    private String subNumber;
    private String sellBuyType;
    private String code;
    private Double stopPrice;

    public void validate() {
        new CombineValidator()
                .add(new StringValidator("accountNumber", this.accountNumber))
                .check();
    }

    public SellBuyTypeEnum getSellBuyType() {
        return this.sellBuyType == null ? null : SellBuyTypeEnum.valueOf(this.sellBuyType);
    }
}
