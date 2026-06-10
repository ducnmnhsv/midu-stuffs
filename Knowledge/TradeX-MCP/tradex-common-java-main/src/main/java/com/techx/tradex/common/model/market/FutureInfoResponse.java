package com.techx.tradex.common.model.market;

import lombok.Data;

import java.util.List;

@Data
public class FutureInfoResponse {
    private String code;
    private Double last;
    private Double open;
    private Double high;
    private Double low;
    private Double change;
    private Double rate;
    private Long tradingVolume;
    private Long tradingValue;
    private String ceilingFloorEqual;
    private String market;
    private String time;
    private String lastTradingTime;
    private Double ceilingPrice;
    private Double floorPrice;
    private Double referencePrice;
    private Double averagePrice;
    private String highTime;
    private String lowTime;
    private Long normalVolume;
    private Double normalValue;
    private Long ptVolume;
    private Double ptValue;
    private Long priorVolume;
    private Double turnoverRate;
    private Double parValue;
    private Long foreignerBidVolume;
    private Double foreignerBidValue;
    private Long foreignerOfferVolume;
    private Double foreignerOfferValue;
    private Long foreignerBidPtVolume;
    private Double foreignerBidPtValue;
    private Long foreignerOfferPtVolume;
    private Double foreignerOfferPtValue;
    private Double bidPrice;
    private Double offerPrice;
    private List<BidOfferItem> bidOfferList = null;
    private Long totalOfferVolume;
    private Long totalOfferCount;
    private Long totalBidVolume;
    private Long totalBidCount;
    private Double highPrice52Weeks;
    private Double lowPrice52Weeks;
    private String firstTradeDate;
    private String endTradeDate;
    private Integer remainingDays;
    private Double mBasis;
    private Double tBasis;
    private Double tPrice;
    private Double disparity;
    private Double disparityRate;
    private Long openInterest;
    private Long openInterestChange;
    private String baseCode;
    private Double expectedPrice;
    private String session;
}
