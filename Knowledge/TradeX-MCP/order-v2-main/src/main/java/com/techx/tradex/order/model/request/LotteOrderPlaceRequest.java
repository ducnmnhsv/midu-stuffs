package com.techx.tradex.order.model.request;

import com.difisoft.market.model.constant.MarketTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.constants.OrderTypeEnum;
import com.difisoft.model.constants.SecurityTypeEnum;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.constants.StopOrderTypeEnum;
import com.difisoft.model.requests.DataRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.order.model.db.ProfitLossOrder;
import com.techx.tradex.order.model.db.StopOrder;
import com.techx.tradex.order.model.db.TrailingOrder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LotteOrderPlaceRequest extends DataRequest {
    private String accountNumber;
    private String subNumber;
    private String stockCode;
    private Double orderPrice;
    private Long orderQuantity;
    private SecurityTypeEnum securitiesType;
    private SellBuyTypeEnum sellBuyType;
    private String bankAccount;
    private String bankName;
    private String bankCode;
    private OrderTypeEnum orderType;
    private String deviceUniqueId;

    public static LotteOrderPlaceRequest fromTrailingOrder(TrailingOrder trailingOrder, SymbolInfo symbolInfo) {
        LotteOrderPlaceRequest request = new LotteOrderPlaceRequest();
        request.setAccountNumber(trailingOrder.getAccountNumber());
        request.setSubNumber(trailingOrder.getSubNumber());
        request.setStockCode(trailingOrder.getCode());
        request.setOrderQuantity(trailingOrder.getQuantity());
        request.setSellBuyType(trailingOrder.getSellBuyType());
        request.setOrderPrice(trailingOrder.getStopPrice());
        request.setOrderType(OrderTypeEnum.LO);
        request.setSourceIp(request.getSourceIp());
        request.setHeaders(trailingOrder.getHeader());
        request.setBankAccount(trailingOrder.getBankAccount());
        request.setBankName(trailingOrder.getBankName());
        request.setBankCode(trailingOrder.getBankCode());
        request.setSecuritiesType(SecurityTypeEnum.valueOf(symbolInfo.getSecuritiesType()));
        return request;
    }

    public static LotteOrderPlaceRequest fromStopOrder(StopOrder stopOrder, SymbolInfo symbolInfo, boolean isEnableLotteBridge) {
        OrderTypeEnum orderType = OrderTypeEnum.LO;
        Double orderPrice = stopOrder.getOrderPrice(); // STOP: stopPrice, STOP_LIMIT: orderPrice
        if (stopOrder.getOrderType().equals(StopOrderTypeEnum.STOP)) {
            if (symbolInfo.getMarketType().equals(MarketTypeEnum.HNX.name())) {
                orderType = OrderTypeEnum.MTL;
                if (isEnableLotteBridge) {
                    orderPrice = stopOrder.getOrderPrice() != null ? stopOrder.getOrderPrice() : 0;
                } else {
                    orderPrice = stopOrder.getStopPrice();
                }
            } else if (symbolInfo.getMarketType().equals(MarketTypeEnum.UPCOM.name())) {
                orderType = OrderTypeEnum.LO;
                if (isEnableLotteBridge) {
                    orderPrice = stopOrder.getOrderPrice() != null ? stopOrder.getOrderPrice() : 0;
                } else {
                    orderPrice = stopOrder.getSellBuyType().equals(SellBuyTypeEnum.BUY) ? symbolInfo.getCeilingPrice() : symbolInfo.getFloorPrice();
                }
            } else if (symbolInfo.getMarketType().equals(MarketTypeEnum.HOSE.name())) {
                orderType = OrderTypeEnum.MP;
                if (isEnableLotteBridge) {
                    orderPrice = stopOrder.getOrderPrice() != null ? stopOrder.getOrderPrice() : 0;
                } else {
                    orderPrice = stopOrder.getStopPrice();
                }
            }
        }
        LotteOrderPlaceRequest request = new LotteOrderPlaceRequest();
        if (stopOrder.getHeader() != null
                && stopOrder.getHeader().getToken() != null
                && stopOrder.getHeader().getToken().getPlatform() != null) {
            String platform = stopOrder.getHeader().getToken().getPlatform();
            stopOrder.getHeader().setPlatform(platform);
            request.setPlatform(platform);
        }
        request.setAccountNumber(stopOrder.getAccountNumber());
        request.setSubNumber(stopOrder.getSubNumber());
        request.setStockCode(stopOrder.getCode());
        request.setOrderQuantity(stopOrder.getQuantity());
        request.setSellBuyType(stopOrder.getSellBuyType());
        request.setOrderPrice(orderPrice);
        request.setOrderType(orderType);
        request.setSourceIp(stopOrder.getSourceIp());
        request.setHeaders(stopOrder.getHeader());
        request.setBankAccount(stopOrder.getBankAccount());
        request.setBankName(stopOrder.getBankName());
        request.setBankCode(stopOrder.getBankCode());
        request.setSecuritiesType(SecurityTypeEnum.valueOf(symbolInfo.getSecuritiesType()));
        request.setDeviceUniqueId(stopOrder.getDeviceUniqueId());
        return request;
    }

    public static LotteOrderPlaceRequest fromProfitLossOrder(ProfitLossOrder plOrder, SymbolInfo symbolInfo) {
        LotteOrderPlaceRequest request = new LotteOrderPlaceRequest();
        request.setAccountNumber(plOrder.getAccountNumber());
        request.setSubNumber(plOrder.getSubNumber());
        request.setStockCode(plOrder.getCode());
        request.setOrderQuantity(plOrder.getQuantity());
        request.setSellBuyType(plOrder.getSellBuyType());
        request.setOrderPrice(plOrder.getOrderPrice());
        request.setOrderType(OrderTypeEnum.LO);
        request.setSourceIp(request.getSourceIp());
        request.setHeaders(plOrder.getHeader());
        request.setBankAccount(plOrder.getBankAccount());
        request.setBankName(plOrder.getBankName());
        request.setBankCode(plOrder.getBankCode());
        request.setSecuritiesType(SecurityTypeEnum.valueOf(symbolInfo.getSecuritiesType()));
        return request;
    }
}
