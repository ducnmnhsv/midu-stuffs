package com.techx.tradex.order.model.request;

import com.difisoft.model.requests.DataRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.order.model.db.ProfitLossOrder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MasBosOrderCancelRequest extends DataRequest {
    private String accountNo;
    private List<OrderCancelItem> orders;

    private String tradingAccSeq;
    private String clientID;


    public static MasBosOrderCancelRequest fromProfitLossOrder(ProfitLossOrder profitLossOrder) {
        MasBosOrderCancelRequest request = new MasBosOrderCancelRequest();
        request.setAccountNo(profitLossOrder.getAccountNumber());
        request.setTradingAccSeq(profitLossOrder.getTradingAccSeq());
        request.setClientID(profitLossOrder.getUsername());
        request.setSourceIp(request.getSourceIp());

        OrderCancelItem orderCancelItem = new OrderCancelItem();
        orderCancelItem.setOrderNo(profitLossOrder.getOrderNumber());
        orderCancelItem.setOrderGroupNo(profitLossOrder.getOrderGroupNumber());
        orderCancelItem.setSymbolCode(profitLossOrder.getCode());
        orderCancelItem.setSellBuyType(profitLossOrder.getSellBuyType().name());
        List<OrderCancelItem> orders = new ArrayList<>();
        orders.add(orderCancelItem);

        request.setOrders(orders);
        return request;
    }

    @Data
    static class OrderCancelItem {
        private String orderNo;
        private String orderGroupNo;
        private String symbolCode;
        private String orderType = "L";
        private String sellBuyType;
    }


}
