package com.techx.tradex.common.model.market;


import lombok.Data;

import java.util.List;

@Data
public class StockInfoResponse {
    private String code;
    private double open;
    private double high;
    private double low;
    private double last;
    private double change;
    private double rate;
    private long tradingVolume;
    private double tradingValue;
    private String companyName;
    private String companyNameEn;
    private String market;
    private String industry;
    private String time;
    private String lastTradingTime;
    private double bidPrice;
    private double offerPrice;
    private double ceilingPrice;
    private double floorPrice;
    private double referencePrice;
    private double averagePrice;
    private String highTime;
    private String lowTime;
    private long normalVolume;
    private double normalValue;
    private long ptVolume;
    private double ptValue;
    private long priorVolume;
    private double turnoverRate;
    private double parValue;
    private long listedQuantity;
    private long foreignerTotalRoom;
    private long foreignerCurrentRoom;
    private long foreignerBuyVolume;
    private long foreignerSellVolume;
    private long totalOfferVolume;
    private long totalOfferCount;
    private long totalBidVolume;
    private long totalBidCount;
    private String rights;
    private String session;
    private double highPrice52Weeks;
    private double lowPrice52Weeks;
    private double expectedPrice;
    private List<BidOfferItem> bidOfferList;
}