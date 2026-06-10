package com.techx.tradex.order.model.request;

import com.difisoft.model.constants.BullBearOrderStatusEnum;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.techx.tradex.order.utils.Utils;
import lombok.Data;

@Data
public class BullBearOrderTodayRequest extends QueryByTimeAndLimitRequest {
    private String accountNumber;
    private String code;
    private String sellBuyType;
    private String status;

    public SellBuyTypeEnum getSellBuyType() {
        return this.sellBuyType == null ? null : SellBuyTypeEnum.valueOf(this.sellBuyType);
    }

    @Override
    public String getUsername() {
        return Utils.getRawUserName(this);
    }

    public BullBearOrderStatusEnum getStatus() {
        return this.status == null ? null : BullBearOrderStatusEnum.valueOf(this.status);
    }
}
