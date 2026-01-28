package com.techx.tradex.common.model.market;

import lombok.Data;

@Data
public class BidOfferItem {
    private double bidPrice;
    private long bidVolume;
    private long bidVolumeChange;
    private double offerPrice;
    private long offerVolume;
    private long offerVolumeChange;
}
