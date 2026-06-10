package com.techx.tradex.order.model.request;

import com.difisoft.model.constants.SellBuyTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrailingOrderHistoryRequest extends QueryByTimeAndLimitRequest {
    private String accountNumber;
    private String code;
    private String sellBuyType;
    private String status;
    private String name;
    private Long lastTrailingOrderId;

    public SellBuyTypeEnum getSellBuyType() {
        return this.sellBuyType == null ? null : SellBuyTypeEnum.valueOf(this.sellBuyType);
    }
}
