package com.techx.tradex.order.model.realtime;

import com.difisoft.model.utils.DefaultUtils;
import com.techx.tradex.order.model.db.StopOrder;
import lombok.Data;

@Data
public class UpdatedOrder {
    private String username;
    private long id;
    private String orderNumber;
    private String accountNumber;
    private String symbolCode;
    private String sellBuyType;
    private String orderType;
    private long quantity;
    private Double stopPrice;
    private Double limitPrice;
    private String status;
    private String orderTime;
    private String fromDate;
    private String toDate;
    private String failMessage;
    private String bankAccount;
    private String bankName;
    private String bankCode;

    public static UpdatedOrder fromStopOrder(StopOrder stopOrder) {
        UpdatedOrder updatedOrder = new UpdatedOrder();
        updatedOrder.setId(stopOrder.getId());
        updatedOrder.setUsername(stopOrder.getUsername());
        updatedOrder.setAccountNumber(stopOrder.getAccountNumber());
        updatedOrder.setSymbolCode(stopOrder.getCode());
        updatedOrder.setOrderNumber(stopOrder.getOrderNumber());
        updatedOrder.setSellBuyType(stopOrder.getSellBuyType().name());
        updatedOrder.setOrderType(stopOrder.getOrderType().name());
        updatedOrder.setQuantity(stopOrder.getQuantity());
        updatedOrder.setStopPrice(stopOrder.getStopPrice());
        updatedOrder.setLimitPrice(stopOrder.getOrderPrice());
        updatedOrder.setStatus(stopOrder.getStatus().name());
        updatedOrder.setOrderTime(DefaultUtils.formatDateTime(stopOrder.getOrderedAt()));
        updatedOrder.setFromDate(DefaultUtils.formatDateTime(stopOrder.getFromDate()));
        updatedOrder.setToDate(DefaultUtils.formatDateTime(stopOrder.getToDate()));
        updatedOrder.setFailMessage(stopOrder.getFailReason());
        updatedOrder.setBankAccount(stopOrder.getBankAccount());
        updatedOrder.setBankCode(stopOrder.getBankCode());
        updatedOrder.setBankName(stopOrder.getBankName());
        return updatedOrder;
    }
}
