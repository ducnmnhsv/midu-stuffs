package com.techx.tradex.order.model.request;

import com.difisoft.model.constants.BullBearOrderStatusEnum;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.validator.CombineValidator;
import com.difisoft.model.utils.validator.EnumValidator;
import com.difisoft.model.utils.validator.NumberValidator;
import com.difisoft.model.utils.validator.StringValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.order.model.db.BullBearOrder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BullBearOrderPlaceRequest extends DataRequest {
    private String accountNumber;
    private String code;
    private Long quantity;
    private String sellBuyType;
    private Double price;
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
                .add(new NumberValidator<>("price", this.price))
                .add(new NumberValidator<>("profitPrice", this.profitPrice))
                .add(new NumberValidator<>("triggerLossPrice", this.triggerLossPrice))
                .add(new NumberValidator<>("toler", this.toler))
                .check();
    }

    public BullBearOrder toBullBearOrder() {
        BullBearOrder bullBearOrder = new BullBearOrder();
        bullBearOrder.setQuantity(this.getQuantity());
        bullBearOrder.setAccountNumber(this.getAccountNumber());
        bullBearOrder.setCode(this.getCode());
        bullBearOrder.setSellBuyType(this.getSellBuyType());
        bullBearOrder.setOrderPrice(this.getPrice());
        bullBearOrder.setProfitPrice(this.getProfitPrice());
        bullBearOrder.setTriggerLossPrice(this.getTriggerLossPrice());
        bullBearOrder.setToler(this.getToler());
        bullBearOrder.setStatus(BullBearOrderStatusEnum.PENDING);
        bullBearOrder.setUsername(this.getUsername());
        bullBearOrder.setSourceIp(this.getSourceIp());
        bullBearOrder.setHeader(this.getHeaders());
        return bullBearOrder;
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
