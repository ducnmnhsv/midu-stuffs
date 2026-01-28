package com.techx.tradex.order.model.response;

import com.difisoft.model.utils.DefaultUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.techx.tradex.order.model.db.TrailingOrder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrailingOrderHistoryResponse {
    private Long trailingOrderId;
    private String code;
    private Long quantity;
    private String sellBuyType;
    private double trailingAmount;
    private double limitOffset;
    private double currentPrice;
    private double stopPrice;
    private String orderNumber;
    private String status;
    private String createTime;
    private String cancelTime;
    private String orderTime;
    private String errorCode;

    public static TrailingOrderHistoryResponse fromTrailingOrder(TrailingOrder trailingOrder) {
        TrailingOrderHistoryResponse response = new TrailingOrderHistoryResponse();
        response.setTrailingOrderId(trailingOrder.getId());
        response.setCode(trailingOrder.getCode());
        response.setQuantity(trailingOrder.getQuantity());
        response.setSellBuyType(trailingOrder.getSellBuyType().name());
        response.setTrailingAmount(trailingOrder.getTrailingAmount());
        response.setLimitOffset(trailingOrder.getLimitOffset());
        response.setCurrentPrice(trailingOrder.getCurrentPrice());
        response.setStopPrice(trailingOrder.getStopPrice());
        response.setOrderNumber(trailingOrder.getOrderNumber());
        response.setStatus(trailingOrder.getStatus().name());
        if (trailingOrder.getOrderedAt() != null) {
            response.setOrderTime(DefaultUtils.DATETIME_FORMAT().format(trailingOrder.getOrderedAt()));
        }
        if (trailingOrder.getCancelledAt() != null) {
            response.setCancelTime(DefaultUtils.DATETIME_FORMAT().format(trailingOrder.getCancelledAt()));
        }
        response.setCreateTime(DefaultUtils.DATETIME_FORMAT().format(trailingOrder.getCreatedAt()));
        response.setErrorCode(trailingOrder.getErrorCode());
        return response;
    }
}
