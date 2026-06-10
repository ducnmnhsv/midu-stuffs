package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;
import org.apache.commons.math3.util.Precision;

@Data
public class MarketStockForeignerResponse {

    private int last;
    private int open;
    private int high;
    private int low;
    private int change;
    private double rate;
    private long tradingVolume;
    private long tradingValue;
    private String date;
    private long foreignerBuyVolume;
    private long foreignerSellVolume;
    private long foreignerTotalRoom;
    private long foreignerCurrentRoom;
    private double foreignerBuyAbleRatio;
    private double foreignerHoldRatio;
    private int foreignerChangeVolume;
    private long foreignerHoldVolume;

    public void setRate(double rate) {
        this.rate = Precision.round(rate, 2);
    }

    public void setForeignerBuyAbleRatio(double foreignerBuyAbleRatio) {
        this.foreignerBuyAbleRatio = Precision.round(foreignerBuyAbleRatio, 2);
    }

    public void setForeignerHoldRatio(double foreignerHoldRatio) {
        this.foreignerHoldRatio = Precision.round(foreignerHoldRatio, 2);
    }
}
