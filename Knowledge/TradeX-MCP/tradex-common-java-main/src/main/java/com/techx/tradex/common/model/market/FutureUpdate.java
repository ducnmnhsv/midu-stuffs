package com.techx.tradex.common.model.market;


import lombok.Data;

@Data
public class FutureUpdate implements IMarketData {
    private String code;
    private String time;
    private String refCode;
    private String highTime;
    private String lowTime;
    private double ceilingPrice;
    private double floorPrice;
    private double open;
    private double high;
    private double low;
    private double last;
    private String filler;
    private double change;
    private double referencePrice;
    private double averagePrice;
    private double basis;
    private double mBasis;
    private double tBasis;
    private double tPrice;
    private double disparity;
    private double disparityRate;
    private double rate;
    private int matchingVolume;
    private String filler2;
    private long tradingVolume;
    private long tradingValue;
    private double bidPrice;
    private double offerPrice;
    private String filler3;
    private int bidVolume;
    private int offerVolume;
    private long totalBidVolume;
    private long totalBidCount;
    private long totalOfferVolume;
    private long totalOfferCount;
    private long foreignerBuyVolume;
    private long foreignerSellVolume;
    private int sequence;
    private String matchedBy;
    private String ceilingFloorEqual;
}
