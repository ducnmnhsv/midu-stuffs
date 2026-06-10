package com.techx.tradex.order.model.request;

import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.constants.TrailingOrderStatusEnum;
import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.validator.CombineValidator;
import com.difisoft.model.utils.validator.EnumValidator;
import com.difisoft.model.utils.validator.NumberValidator;
import com.difisoft.model.utils.validator.StringValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.order.model.db.TrailingOrder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrailingOrderPlaceRequest extends DataRequest {
    private String accountNumber;
    private String code;
    private Long quantity;
    private String sellBuyType;
    private Double trailingAmount;
    private Double limitOffset;

    public void validate() {
        new CombineValidator()
                .add(new StringValidator("accountNumber", this.accountNumber).empty())
                .add(new StringValidator("code", this.code).empty())
                .add(new NumberValidator<>("quantity", this.quantity))
                .add(new StringValidator("sellBuyType", this.sellBuyType).empty())
                .add(new EnumValidator<>("sellBuyType", this.sellBuyType, SellBuyTypeEnum.class))
                .add(new NumberValidator<>("trailingAmount", this.trailingAmount))
                .add(new NumberValidator<>("limitOffset", this.limitOffset))
                .check();
    }

    public TrailingOrder toTrailingOrder() {
        TrailingOrder trailingOrder = new TrailingOrder();
        trailingOrder.setQuantity(this.getQuantity());
        trailingOrder.setAccountNumber(this.getAccountNumber());
        trailingOrder.setCode(this.getCode());
        trailingOrder.setSellBuyType(this.getSellBuyType());
        trailingOrder.setTrailingAmount(this.getTrailingAmount());
        trailingOrder.setLimitOffset(this.getLimitOffset());
        trailingOrder.setStatus(TrailingOrderStatusEnum.PENDING);
        trailingOrder.setUsername(this.getUsername());
        trailingOrder.setSourceIp(this.getSourceIp());
        trailingOrder.setHeader(this.getHeaders());
        return trailingOrder;
    }

    public SellBuyTypeEnum getSellBuyType() {
        return this.sellBuyType == null ? null : SellBuyTypeEnum.valueOf(this.sellBuyType);
    }
}
