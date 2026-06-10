package com.techx.tradex.order.model.response;

import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.responses.Status;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.model.db.OcoOrder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class OcoOrderDetailResponse {
    private Long ocoOrderId;
    private String accountNumber;
    private String code;
    private Long orderQuantity;
    private Long matchedQuantity;
    private Long unmatchedQuantity;
    private Long cancelledQuantity;
    private String sellBuyType;
    private Double profitPrice;
    private Double triggerLossPrice;
    private Double lossPrice;
    private Double toler;
    private Double matchedPrice;
    private String status;
    private String orderTime;
    private List<ProfitLossOrderResponse> profitLossOrders;

    @Data
    private static class ProfitLossOrderResponse {
        private Long plId;
        private String code;
        private Long quantity;
        private Long matchedQuantity;
        private Long unmatchedQuantity;
        private Double price;
        private String status;
        private String profitLossType;
        private String tradingAccSeq;
        private String orderId;
        private String orderGroupId;
        private String failReason;
        private String cancelledAt;
        private String cancelledBy;
        private String createdAt;
    }

    public static OcoOrderDetailResponse fromOcoOrder(ObjectMapper objectMapper, OcoOrder ocoOrder) {
        OcoOrderDetailResponse response = new OcoOrderDetailResponse();
        response.setOcoOrderId(ocoOrder.getId());
        response.setAccountNumber(ocoOrder.getAccountNumber());
        response.setCode(ocoOrder.getCode());
        response.setOrderQuantity(ocoOrder.getQuantity());
        response.setSellBuyType(ocoOrder.getSellBuyType().name());
        if (ocoOrder.getOrderedAt() != null) {
            response.setOrderTime(DefaultUtils.TIME_DATE_FORMAT().format(ocoOrder.getOrderedAt()));
        }
        response.setStatus(ocoOrder.getStatus() == null ? null : ocoOrder.getStatus().name());
        response.setMatchedQuantity(ocoOrder.getMatchQuantity());
        response.setUnmatchedQuantity(ocoOrder.getUnmatchQuantity());
        response.setToler(ocoOrder.getToler());
        response.setProfitPrice(ocoOrder.getProfitPrice());
        response.setTriggerLossPrice(ocoOrder.getTriggerLossPrice());
        response.setLossPrice(SellBuyTypeEnum.BUY.equals(ocoOrder.getSellBuyType())
                ? ocoOrder.getTriggerLossPrice() + ocoOrder.getToler()
                : ocoOrder.getTriggerLossPrice() - ocoOrder.getToler()
        );
        response.setProfitLossOrders(ocoOrder.getProfitLossOrders().stream().map(order -> {
                    ProfitLossOrderResponse plOrder = new ProfitLossOrderResponse();
                    plOrder.setPlId(order.getId());
                    plOrder.setCode(order.getCode());
                    plOrder.setQuantity(order.getQuantity());
                    plOrder.setMatchedQuantity(order.getMatchQuantity());
                    plOrder.setUnmatchedQuantity(order.getQuantity() - order.getMatchQuantity());
                    plOrder.setPrice(order.getOrderPrice());
                    plOrder.setStatus(order.getStatus() == null ? null : order.getStatus().name());
                    plOrder.setProfitLossType(order.getProfitLossType() == null ? null : order.getProfitLossType().name());
                    plOrder.setTradingAccSeq(order.getTradingAccSeq());
                    plOrder.setOrderId(order.getOrderNumber());
                    plOrder.setOrderGroupId(order.getOrderGroupNumber());
                    plOrder.setFailReason(order.getFailReason());
                    plOrder.setCancelledAt(order.getCancelledAt() == null ? null : DefaultUtils.TIME_DATE_FORMAT().format(order.getCancelledAt()));
                    plOrder.setCancelledBy(order.getCancelledBy());
                    plOrder.setCreatedAt(order.getCreatedAt() == null ? null : DefaultUtils.TIME_DATE_FORMAT().format(order.getCreatedAt()));
                    if (StringUtils.isNotBlank(order.getFailReason())) {
                        try {
                            Status status = objectMapper.readValue(order.getFailReason().replaceAll("^\"|\"$|\\\\", ""), Status.class);
                            plOrder.setFailReason(status.getCode());
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                    return plOrder;
                }).collect(Collectors.toList())
        );

        return response;
    }
}
