package com.techx.tradex.order.model.response;

import com.difisoft.model.responses.Status;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.model.db.OcoOrder;
import lombok.Data;

@Data
public class OcoOrderHistoryResponse {
    private Long id;
    private String accountNumber;
    private String code;
    private Long orderQuantity;
    private String sellBuyType;
    private Double profitPrice;
    private Double triggerLossPrice;
    private String status;
    private String createTime;
    private String orderTime;
    private String cancelTime;
    private String errorMessage;
    private Long matchedQuantity;
    private Long unmatchedQuantity;
    private Double toler;

    public static OcoOrderHistoryResponse fromOcoOrder(ObjectMapper objectMapper, OcoOrder ocoOrder) {
        OcoOrderHistoryResponse response = new OcoOrderHistoryResponse();
        response.setId(ocoOrder.getId());
        response.setAccountNumber(ocoOrder.getAccountNumber());
        response.setCode(ocoOrder.getCode());
        response.setOrderQuantity(ocoOrder.getQuantity());
        response.setSellBuyType(ocoOrder.getSellBuyType() == null ? null : ocoOrder.getSellBuyType().name());
        response.setCreateTime(DefaultUtils.TIME_DATE_FORMAT().format(ocoOrder.getCreatedAt()));
        if (ocoOrder.getOrderedAt() != null) {
            response.setOrderTime(DefaultUtils.TIME_DATE_FORMAT().format(ocoOrder.getOrderedAt()));
        }
        if (ocoOrder.getCancelledAt() != null) {
            response.setCancelTime(DefaultUtils.TIME_DATE_FORMAT().format(ocoOrder.getCancelledAt()));
        }
        response.setStatus(ocoOrder.getStatus() == null ? null : ocoOrder.getStatus().name());
        response.setMatchedQuantity(ocoOrder.getMatchQuantity());
        response.setUnmatchedQuantity(ocoOrder.getUnmatchQuantity());
        response.setToler(ocoOrder.getToler());
        response.setProfitPrice(ocoOrder.getProfitPrice());
        response.setTriggerLossPrice(ocoOrder.getTriggerLossPrice());
        if (StringUtils.isNotBlank(ocoOrder.getFailReason())) {
            try {
                Status status = objectMapper.readValue(ocoOrder.getFailReason().replaceAll("^\"|\"$|\\\\", ""), Status.class);
                response.setErrorMessage(status.getCode());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}
