package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;
import org.apache.commons.math3.util.Precision;

@Data
public class MarketStockPeriodResponse {
    private double last;
    private double open;
    private double high;
    private double low;
    private double change;
    private double rate;
    private long tradingVolume;
    private double tradingValue;
    private String date;
    private long dayCount;
    private long holdVolume;
    private double holdRatio;
    private double buyAbleRatio;

    public void setLast(double last) {
        this.last = Precision.round(last, 2);
    }

    public void setOpen(double open) {
        this.open = Precision.round(open, 2);
    }

    public void setHigh(double high) {
        this.high = Precision.round(high, 2);
    }

    public void setLow(double low) {
        this.low = Precision.round(low, 2);
    }

    public void setChange(double change) {
        this.change = Precision.round(change, 2);
    }

    public void setTradingValue(double tradingValue) {
        this.tradingValue = Precision.round(tradingValue, 2);
    }

    public void setRate(double rate) {
        this.rate = Precision.round(rate, 2);
    }

    public void setHoldRatio(double holdRatio) {
        this.holdRatio = Precision.round(holdRatio, 2);
    }

    public void setBuyAbleRatio(double buyAbleRatio) {
        this.buyAbleRatio = Precision.round(buyAbleRatio, 2);
    }

}

