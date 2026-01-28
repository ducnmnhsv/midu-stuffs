package com.techx.tradex.order.model.request;

import com.difisoft.model.constants.OcoOrderStatusEnum;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.validator.CombineValidator;
import com.difisoft.model.utils.validator.EnumValidator;
import com.difisoft.model.utils.validator.NumberValidator;
import com.difisoft.model.utils.validator.StringValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.order.model.db.OcoOrder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OcoOrderPlaceRequest extends DataRequest {
    private String accountNumber;
    private String code;
    private Long quantity;
    private String sellBuyType;
    private Double profitPrice;
    private Double triggerLossPrice;
    private Double toler;

    public void validate() {
        new CombineValidator()
                .add(new StringValidator("accountNumber", this.accountNumber).empty())
                .add(new StringValidator("code", this.code).empty())
                .add(new NumberValidator<>("quantity", this.quantity))
                .add(new StringValidator("sellBuyType", this.sellBuyType).empty())
                .add(new EnumValidator<>("sellBuyType", this.sellBuyType, SellBuyTypeEnum.class))
                .add(new NumberValidator<>("profitPrice", this.profitPrice))
                .add(new NumberValidator<>("triggerLossPrice", this.triggerLossPrice))
                .add(new NumberValidator<>("toler", this.toler))
                .check();
    }

    public OcoOrder toOcoOrder() {
        OcoOrder ocoOrder = new OcoOrder();
        ocoOrder.setQuantity(this.getQuantity());
        ocoOrder.setMatchQuantity(0);
        ocoOrder.setUnmatchQuantity(this.getQuantity());
        ocoOrder.setAccountNumber(this.getAccountNumber());
        ocoOrder.setCode(this.getCode());
        ocoOrder.setSellBuyType(this.getSellBuyType());
        ocoOrder.setProfitPrice(this.getProfitPrice());
        ocoOrder.setTriggerLossPrice(this.getTriggerLossPrice());
        ocoOrder.setToler(this.getToler());
        ocoOrder.setStatus(OcoOrderStatusEnum.PENDING);
        ocoOrder.setUsername(this.getUsername());
        ocoOrder.setSourceIp(this.getSourceIp());
        ocoOrder.setHeader(this.getHeaders());
        return ocoOrder;
    }

    public String getUsername() {
        if (this.getHeaders() != null
                && this.getHeaders().getToken() != null
                && this.getHeaders().getToken().getUserData() != null
                && this.getHeaders().getToken().getUserData().getUsername() != null) {
            return this.getHeaders().getToken().getUserData().getUsername();
        }
        return null;
    }

    public SellBuyTypeEnum getSellBuyType() {
        return this.sellBuyType == null ? null : SellBuyTypeEnum.valueOf(this.sellBuyType);
    }
}
