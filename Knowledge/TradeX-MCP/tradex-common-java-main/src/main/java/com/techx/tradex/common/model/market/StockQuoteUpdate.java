package com.techx.tradex.common.model.market;

import lombok.Data;

@Data
public class StockQuoteUpdate implements IMarketData {
    private String code;
    private String time;
    private double open;
    private double high;
    private double low;
    private double last;
    private double change;
    private double rate;
    private String highTime;
    private String lowTime;
    private double bidPrice;
    private double offerPrice;
    private double ceilingPrice;
    private double floorPrice;
    private double averagePrice;
    private double referencePrice;
    private long tradingVolume;
    private long tradingValue;
    private double turnoverRate;
    private long matchingVolume;
    private long bidVolume;
    private long offerVolume;
    private long totalBidVolume;
    private long totalBidCount;
    private long totalOfferVolume;
    private long foreignerBuyVolume;
    private long foreignerSellVolume;
    private long foreignerTotalRoom;
    private long foreignerCurrentRoom;
    private long holdVolume;
    private double holdRatio;
    private double buyAbleRatio;
    private String matchedBy;
    private long sequence;
}
