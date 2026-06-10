package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;
import org.apache.commons.math3.util.Precision;

@Data
public class MarketStockRankingUpDownResponse {
    private String code;
    private int last;
    protected int open;
    protected int high;
    protected int low;
    private int change;
    private String ceilingFloorEqual;
    private double rate;
    private long tradingVolume;
    private int upDownChange;
    private double upDownRate;
    private int startPrice;
    private int endPrice;

    public void setRate(double rate) {
        this.rate = Precision.round(rate, 2);
    }

    public void setUpDownRate(double upDownRate) {
        this.upDownRate = Precision.round(upDownRate, 2);
    }

}
