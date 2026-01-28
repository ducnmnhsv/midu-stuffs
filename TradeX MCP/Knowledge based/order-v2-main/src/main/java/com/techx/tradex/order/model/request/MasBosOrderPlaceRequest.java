package com.techx.tradex.order.model.request;

import com.difisoft.market.model.constant.MarketTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.constants.OrderTypeEnum;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.constants.StopOrderTypeEnum;
import com.difisoft.model.requests.DataRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.order.model.db.ProfitLossOrder;
import com.techx.tradex.order.model.db.StopOrder;
import com.techx.tradex.order.model.db.TrailingOrder;
import lombok.Data;

import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MasBosOrderPlaceRequest extends DataRequest {
    private String accountNumber;
    private String sellBuyType;
    private String code;
    private String expiryDate;
    private String marketType;
    private long orderQuantity;
    private String orderType;
    private double orderPrice;
    private String tradingAccSeq;
    private String clientID;
    private String remark;
    private String macAddress;
    private String deviceUniqueId;


    public static MasBosOrderPlaceRequest fromTrailingOrder(TrailingOrder trailingOrder, SymbolInfo symbolInfo) {
        MasBosOrderPlaceRequest request = new MasBosOrderPlaceRequest();
        request.setAccountNumber(trailingOrder.getAccountNumber());
        request.setCode(trailingOrder.getCode());
        request.setOrderQuantity(trailingOrder.getQuantity());
        request.setOrderPrice(trailingOrder.getStopPrice());
        request.setSellBuyType(trailingOrder.getSellBuyType().name());
        request.setOrderType(OrderTypeEnum.LO.name());
        request.setSourceIp(request.getSourceIp());
        request.setHeaders(trailingOrder.getHeader());
        request.setClientID(trailingOrder.getUsername());
        request.setMarketType(symbolInfo.getMarketType());
        return request;
    }


    public static MasBosOrderPlaceRequest fromStopOrder(StopOrder stopOrder, SymbolInfo symbolInfo) {
        String orderType = OrderTypeEnum.LO.name();
        Double orderPrice = stopOrder.getOrderPrice(); // STOP: stopPrice, STOP_LIMIT: orderPrice
        if (stopOrder.getOrderType().equals(StopOrderTypeEnum.STOP)) {
            if (symbolInfo.getMarketType().equals(MarketTypeEnum.HNX.name())) {
                orderType = OrderTypeEnum.MTL.name();
                orderPrice = stopOrder.getStopPrice();
            } else if (symbolInfo.getMarketType().equals(MarketTypeEnum.UPCOM.name())) {
                orderType = OrderTypeEnum.LO.name();
                orderPrice = stopOrder.getSellBuyType().equals(SellBuyTypeEnum.BUY) ? symbolInfo.getCeilingPrice() : symbolInfo.getFloorPrice();
            } else if (symbolInfo.getMarketType().equals(MarketTypeEnum.HOSE.name())) {
                orderType = OrderTypeEnum.MP.name();
                orderPrice = stopOrder.getStopPrice();
            }
        }

        MasBosOrderPlaceRequest request = new MasBosOrderPlaceRequest();
        if (stopOrder.getHeader() != null
                && stopOrder.getHeader().getToken() != null
                && stopOrder.getHeader().getToken().getPlatform() != null) {
            String platform = stopOrder.getHeader().getToken().getPlatform();
            stopOrder.getHeader().setPlatform(platform);
            request.setPlatform(platform);
        }
        request.setAccountNumber(stopOrder.getAccountNumber());
        request.setCode(stopOrder.getCode());
        request.setOrderQuantity(stopOrder.getQuantity());
        request.setOrderPrice(orderPrice);
        request.setSellBuyType(stopOrder.getSellBuyType().name());
        request.setOrderType(orderType);
        request.setSourceIp(stopOrder.getSourceIp());
        request.setHeaders(stopOrder.getHeader());
        request.setClientID(stopOrder.getUsername());
        request.setMarketType(symbolInfo.getMarketType());
        request.setRemark(stopOrder.getRemark());
        request.setDeviceUniqueId(stopOrder.getDeviceUniqueId());
        if (!(Objects.nonNull(stopOrder.getDeviceUniqueId()) && Objects.nonNull(stopOrder.getMacAddress()))) {
            request.setMacAddress(stopOrder.getMacAddress());
        }
        return request;
    }

    public static MasBosOrderPlaceRequest fromProfitLossOrder(ProfitLossOrder plOrder, SymbolInfo symbolInfo) {
        MasBosOrderPlaceRequest request = new MasBosOrderPlaceRequest();
        request.setAccountNumber(plOrder.getAccountNumber());
        request.setCode(plOrder.getCode());
        request.setOrderQuantity(plOrder.getQuantity());
        request.setOrderPrice(plOrder.getOrderPrice());
        request.setSellBuyType(plOrder.getSellBuyType().name());
        request.setOrderType(OrderTypeEnum.LO.name());
        request.setSourceIp(request.getSourceIp());
        request.setHeaders(plOrder.getHeader());
        request.setClientID(plOrder.getUsername());
        request.setMarketType(symbolInfo.getMarketType());
        return request;
    }
}
