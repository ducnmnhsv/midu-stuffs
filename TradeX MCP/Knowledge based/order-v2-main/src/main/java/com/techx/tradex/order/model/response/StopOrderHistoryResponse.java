package com.techx.tradex.order.model.response;

import com.difisoft.model.responses.Status;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.constants.Constants;
import com.techx.tradex.order.model.db.StopOrder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Data
@Slf4j
public class StopOrderHistoryResponse {
    private Long stopOrderId;
    private String accountNumber;
    private String code;
    private Long orderQuantity;
    private String sellBuyType;
    private Double stopPrice;
    private Double orderPrice;
    private String orderType;
    private String orderNumber;
    private String status;
    private String createTime;
    private String orderTime;
    private String cancelTime;
    private String errorMessage;
    private String fromDate;
    private String toDate;
    private String channel;

    public static StopOrderHistoryResponse fromStopOrder(StopOrder stopOrder, ObjectMapper objectMapper) {
        StopOrderHistoryResponse response = new StopOrderHistoryResponse();
        response.setStopOrderId(stopOrder.getId());
        response.setAccountNumber(stopOrder.getAccountNumber());
        response.setCode(stopOrder.getCode());
        response.setOrderQuantity(stopOrder.getQuantity());
        response.setSellBuyType(stopOrder.getSellBuyType().name());
        response.setStopPrice(stopOrder.getStopPrice());
        response.setOrderPrice(stopOrder.getOrderPrice());
        response.setOrderType(stopOrder.getOrderType().name());
        response.setOrderNumber(stopOrder.getOrderNumber());
        response.setStatus(stopOrder.getStatus().name());
        response.setCreateTime(DefaultUtils.formatDateTime(stopOrder.getCreatedAt()));
        if (stopOrder.getOrderedAt() != null) {
            response.setOrderTime(DefaultUtils.formatDateTime(stopOrder.getOrderedAt()));
        }
        if (stopOrder.getCancelledAt() != null) {
            response.setCancelTime(DefaultUtils.formatDateTime(stopOrder.getCancelledAt()));
        }
        response.setErrorMessage(stopOrder.getFailReason());
        response.setFromDate(DefaultUtils.formatDate(stopOrder.getFromDate()));
        response.setToDate(DefaultUtils.formatDate(stopOrder.getToDate()));
        if (stopOrder.getHeader() != null && stopOrder.getHeader().getToken() != null) {
            response.setChannel(stopOrder.getHeader().getToken().getPlatform());
        }
        if (StringUtils.isNotBlank(stopOrder.getFailReason()) && !org.apache.commons.lang3.StringUtils.equals(stopOrder.getFailReason(), Constants.NULL)) {
            try {
                Status status = objectMapper.readValue(stopOrder.getFailReason().replaceAll("^\"|\"$|\\\\", ""), Status.class);
                response.setErrorMessage(
                        Objects.nonNull(status) && Objects.nonNull(status.getCode())
                                ? status.getCode()
                                : org.apache.commons.lang3.StringUtils.EMPTY
                );
            } catch (Exception e) {
                log.error("fromStopOrder - stopOrder id = {}, error: ", stopOrder.getId(), e);
            }
        }
        return response;
    }
}
