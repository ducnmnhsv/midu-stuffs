package com.techx.tradex.order.model.request;


import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.constants.StopOrderStatusEnum;
import com.difisoft.model.constants.StopOrderTypeEnum;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.StringUtils;
import com.difisoft.model.utils.validator.CombineValidator;
import com.difisoft.model.utils.validator.EnumValidator;
import com.difisoft.model.utils.validator.NumberValidator;
import com.difisoft.model.utils.validator.StringValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.order.constants.Constants;
import com.techx.tradex.order.model.db.StopOrder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopOrderPlaceRequest extends DataRequest {
    private String accountNumber;
    private String subNumber;
    private String code;
    private Long orderQuantity;
    private String sellBuyType;
    private String orderType;
    private Double stopPrice;
    private Double orderPrice;
    private String fromDate; //optional
    private String toDate; //optional
    private String remark;
    private String macAddress;
    private String deviceUniqueId;
    private String bankCode; // only for lotte
    private String bankAccount; // only for lotte
    private String bankName; // only for lotte

    public void validate() {

        if (!StringUtils.isNotBlank(this.accountNumber)) {
            throw new GeneralException(Constants.ACCOUNT_NUMBER_MUST_BE_REQUIRED);
        }

        if (this.getHeaders().getToken().getUserData().getAccountNumbers().stream()
                .noneMatch(x -> x.equalsIgnoreCase(this.accountNumber))) {
            throw new GeneralException(Constants.UNABLE_TO_STOP_ORDERS_OF_ANOTHER_ACCOUNT);
        }

        if (this.stopPrice == null) {
            this.stopPrice = 0d;
        }
        new CombineValidator()
                .add(new StringValidator("accountNumber", this.accountNumber).empty())
                .add(new StringValidator("code", this.code).empty())
                .add(new NumberValidator<>("orderQuantity", this.orderQuantity))
                .add(new EnumValidator<>("sellBuyType", this.sellBuyType, SellBuyTypeEnum.class))
                .add(new StringValidator("orderType", this.orderType))
                .add(new NumberValidator<>("stopPrice", this.stopPrice))
                .check();
        if (this.getOrderType().equals(StopOrderTypeEnum.STOP_LIMIT.name())) {
            new CombineValidator()
                    .add(new NumberValidator<>("orderPrice", this.getOrderPrice()))
                    .check();
        }
        if (this.getStopPrice() <= 0) {
            throw new GeneralException(Constants.STOP_PRICE_MUST_BE_SET);
        }

    }

    public StopOrder toStopOrder(ZonedDateTime fromDate, ZonedDateTime toDate) {
        StopOrder stopOrder = new StopOrder();
        stopOrder.setAccountNumber(this.getAccountNumber());
        stopOrder.setSubNumber(this.getSubNumber());
        stopOrder.setCode(this.getCode());
        stopOrder.setQuantity(this.getOrderQuantity());
        stopOrder.setSellBuyType(this.getSellBuyType());
        stopOrder.setOrderType(StopOrderTypeEnum.valueOf(this.getOrderType()));
        stopOrder.setStopPrice(this.getStopPrice());
        stopOrder.setOrderPrice(this.getOrderPrice() == null || this.getOrderPrice() == 0 ? null : this.getOrderPrice());
        stopOrder.setStatus(StopOrderStatusEnum.PENDING);
        stopOrder.setFromDate(fromDate);
        stopOrder.setToDate(toDate);
        stopOrder.setUsername(this.getUsername());
        stopOrder.setSourceIp(this.getSourceIp());
        stopOrder.setHeader(this.getHeaders());
        stopOrder.setRemark(this.getRemark());
        stopOrder.setMacAddress(this.getMacAddress());
        stopOrder.setDeviceUniqueId(this.getDeviceUniqueId());
        stopOrder.setBankAccount(this.getBankAccount());
        stopOrder.setBankCode(this.getBankCode());
        stopOrder.setBankName(this.getBankName());
        return stopOrder;
    }

    public SellBuyTypeEnum getSellBuyType() {
        return this.sellBuyType == null ? null : SellBuyTypeEnum.valueOf(this.sellBuyType);
    }

    @Override
    public String getUsername() {
        if (this.getHeaders() != null
                && this.getHeaders().getToken() != null
                && this.getHeaders().getToken().getUserData() != null
                && this.getHeaders().getToken().getUserData().getUsername() != null) {
            return this.getHeaders().getToken().getUserData().getUsername();
        }
        return null;
    }
}
