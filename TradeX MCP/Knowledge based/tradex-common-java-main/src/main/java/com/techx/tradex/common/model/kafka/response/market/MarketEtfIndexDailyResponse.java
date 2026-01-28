package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;
import org.apache.commons.math3.util.Precision;

@Data
public class MarketEtfIndexDailyResponse {

    private String etfCode;
    private String date;
    private double last;
    private double open;
    private double high;
    private double low;
    private double change;
    private double rate;

    public void setLast(float last) {
        this.last = Precision.round(last, 2);
    }

    public void setChange(float change) {
        this.change = Precision.round(change, 2);
    }

    public void setRate(float rate) {
        this.rate = Precision.round(rate, 2);
    }

    public void setOpen(float open) {
        this.open = Precision.round(open, 2);
    }

    public void setHigh(float high) {
        this.high = Precision.round(high, 2);
    }

    public void setLow(float low) {
        this.low = Precision.round(low, 2);
    }

}
