package com.techx.tradex.common.model.market;

import lombok.Data;

import java.util.List;

@Data
public class FutureBidOfferUpdate implements IMarketData {
    private List<BidOfferItem> bidOfferList = null;
    private String code;
    private String time;
    private Double bidPrice;
    private Long bidVolume;
    private Double offerPrice;
    private Long offerVolume;
    private Long totalBidVolume;
    private Long totalOfferVolume;
    private Long totalBidCount;
    private Long totalOfferCount;
    private Double expectedPrice;
    private String session;
}
