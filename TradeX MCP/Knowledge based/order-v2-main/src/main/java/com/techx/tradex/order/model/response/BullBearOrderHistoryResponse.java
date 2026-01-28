package com.techx.tradex.order.model.response;

import com.difisoft.model.responses.Status;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.model.db.BullBearOrder;
import com.techx.tradex.order.model.db.ProfitLossOrder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BullBearOrderHistoryResponse {
    private Long id;
    private Long ocoOrderId;
    private ProfitLossOrderResponse plOrder;
    private String accountNumber;
    private String code;
    private String sellBuyType;
    private Long orderQuantity;
    private Double orderPrice;
    private Double takeProfitPrice;
    private Double triggerLossPrice;
    private Double toler;
    private String orderStatus;
    private Long matchedQuantity;
    private Long unmatchedQuantity;
    private String orderTime;

    @Data
    private static class ProfitLossOrderResponse {
        private Long plId;
        private String code;
        private Long quantity;
        private Long matchedQuantity;
        private Long unmatchedQuantity;
        private Double price;
        private String status;
        private String tradingAccSeq;
        private String orderId;
        private String orderGroupId;
        private String failReason;
        private String cancelledAt;
        private String cancelledBy;
        private String createdAt;
        private Long cancelledQuantity;
    }

    public static BullBearOrderHistoryResponse fromBullBearOrder(ObjectMapper objectMapper, BullBearOrder bullBearOrder) {
        BullBearOrderHistoryResponse response = new BullBearOrderHistoryResponse();
        response.setId(bullBearOrder.getId());
        response.setOcoOrderId(bullBearOrder.getOcoOrder() == null ? null : bullBearOrder.getOcoOrder().getId());
        response.setCode(bullBearOrder.getCode());
        response.setAccountNumber(bullBearOrder.getAccountNumber());
        response.setSellBuyType(bullBearOrder.getSellBuyType() == null ? null : bullBearOrder.getSellBuyType().name());
        response.setOrderQuantity(bullBearOrder.getQuantity());
        response.setOrderPrice(bullBearOrder.getOrderPrice());
        response.setTakeProfitPrice(bullBearOrder.getProfitPrice());
        response.setTriggerLossPrice(bullBearOrder.getTriggerLossPrice());
        response.setToler(bullBearOrder.getToler());
        response.setOrderStatus(bullBearOrder.getStatus() == null ? null : bullBearOrder.getStatus().name());
        response.setMatchedQuantity(bullBearOrder.getMatchQuantity());
        response.setUnmatchedQuantity(bullBearOrder.getQuantity() - bullBearOrder.getMatchQuantity());
        if (bullBearOrder.getOrderedAt() != null) {
            response.setOrderTime(DefaultUtils.DATETIME_FORMAT().format(bullBearOrder.getOrderedAt()));
        }
        ProfitLossOrder plOrder = bullBearOrder.getProfitLossOrder();
        if (plOrder == null) return response;
        ProfitLossOrderResponse plOrderResponse = new ProfitLossOrderResponse();
        plOrderResponse.setPlId(plOrder.getId());
        plOrderResponse.setCode(plOrder.getCode());
        plOrderResponse.setQuantity(plOrder.getQuantity());
        plOrderResponse.setMatchedQuantity(plOrder.getMatchQuantity());
        plOrderResponse.setUnmatchedQuantity(plOrder.getQuantity() - plOrder.getMatchQuantity());
        plOrderResponse.setPrice(plOrder.getOrderPrice());
        plOrderResponse.setStatus(plOrder.getStatus() == null ? null : plOrder.getStatus().name());
        plOrderResponse.setTradingAccSeq(plOrder.getTradingAccSeq());
        plOrderResponse.setOrderId(plOrder.getOrderNumber());
        plOrderResponse.setOrderGroupId(plOrder.getOrderGroupNumber());
        if (StringUtils.isNotBlank(plOrder.getFailReason())) {
            try {
                Status status = objectMapper.readValue(plOrder.getFailReason().replaceAll("^\"|\"$|\\\\", ""), Status.class);
                plOrderResponse.setFailReason(status.getCode());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        plOrderResponse.setCancelledAt(plOrder.getCancelledAt() == null ? null : DefaultUtils.TIME_DATE_FORMAT().format(plOrder.getCancelledAt()));
        plOrderResponse.setCancelledBy(plOrder.getCancelledBy());
        plOrderResponse.setCreatedAt(plOrder.getCreatedAt() == null ? null : DefaultUtils.TIME_DATE_FORMAT().format(plOrder.getCreatedAt()));
        plOrderResponse.setCancelledQuantity(plOrder.getCancelledQuantity());
        response.setPlOrder(plOrderResponse);
        return response;
    }
}
