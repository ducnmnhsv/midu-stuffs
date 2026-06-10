package com.techx.tradex.common.model.market;

import lombok.Data;

import java.util.List;

@Data
public class BidOfferUpdate implements IMarketData {
    private String code;
    private Double time;
    private Double bidPrice;
    private Long bidVolume;
    private Double offerPrice;
    private Long offerVolume;
    private Long totalBidVolume;
    private Long totalOfferVolume;
    private Long totalBidCount;
    private Long totalOfferCount;
    private Double expectedPrice;
    private List<BidOfferItem> bidOfferList;
    private String session;
}
