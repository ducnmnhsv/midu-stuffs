package com.techx.tradex.order.model.request;

import com.difisoft.model.constants.OcoOrderStatusEnum;
import com.difisoft.model.constants.SellBuyTypeEnum;
import lombok.Data;

@Data
public class OcoOrderTodayRequest extends QueryByTimeAndLimitRequest {
    private String accountNumber;
    private String code;
    private String sellBuyType;
    private String status;

    public SellBuyTypeEnum getSellBuyType() {
        return this.sellBuyType == null ? null : SellBuyTypeEnum.valueOf(this.sellBuyType);
    }

    public OcoOrderStatusEnum getStatus() {
        return this.status == null ? null : OcoOrderStatusEnum.valueOf(this.status);
    }
}
