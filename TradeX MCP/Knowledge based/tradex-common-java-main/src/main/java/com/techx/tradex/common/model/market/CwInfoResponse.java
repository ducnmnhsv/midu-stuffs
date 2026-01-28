package com.techx.tradex.common.model.market;

import lombok.Data;

import java.util.List;

@Data
public class CwInfoResponse {
    private String code;
    private Double last;
    private Double open;
    private Double high;
    private Double low;
    private Double change;
    private Double rate;
    private Double ceilingPrice;
    private Double floorPrice;
    private String ceilingFloorEqual;
    private Double referencePrice;
    private Double averagePrice;
    private Double exercisePrice;
    private String exerciseRatio;
    private Double breakEven;
    private Double impliedVolatility;
    private String maturityDate;
    private String lastTradingDate;
    private Double theoreticalPrice;
    private Double delta;
    private String underlyingAssetCode;
    private Double underlyingAssetPrice;
    private Double underlyingAssetRate;
    private String market;
    private String time;
    private String lastTradingTime;
    private String highTime;
    private String lowTime;
    private Long tradingVolume;
    private Double tradingValue;
    private Long ptVolume;
    private Double ptValue;
    private Long priorVolume;
    private Double turnoverRate;
    private Long listedQuantity;
    private Double bidPrice;
    private Double offerPrice;
    private Long totalOfferVolume;
    private Double totalOfferCount;
    private Long totalBidVolume;
    private Double totalBidCount;
    private Double expectedPrice;
    private String session;
    private List<BidOfferItem> bidOfferList;
}
