package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.List;

@Data
public class MarketIndexInfoResponse {
    private double last;
    private double open;
    private double high;
    private double low;
    private double change;
    private double rate;
    private long tradingVolume;
    private double tradingValue;
    private long priorVolume;
    private double ptVolume;
    private Integer upCount;
    private Integer ceilingCount;
    private Integer downCount;
    private Integer floorCount;
    private Integer unchangedCount;
    private String date;
    private List<Session> sessions = new ArrayList<>();

    @Data
    public static class Session {
        private double last;
        private double change;
        private double rate;
        private long tradingVolume;
        private long tradingValue;

        public void setLast(double last) {
            this.last = Precision.round(last, 2);
        }

        public void setChange(double change) {
            this.change = Precision.round(change, 2);
        }

        public void setRate(double rate) {
            this.rate = Precision.round(rate, 2);
        }
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

    public void setLast(double last) {
        this.last = Precision.round(last, 2);
    }

    public void setChange(double change) {
        this.change = Precision.round(change, 2);
    }

    public void setRate(double rate) {
        this.rate = Precision.round(rate, 2);
    }

    public void setPtVolume(double ptVolume) {
        this.ptVolume = Precision.round(ptVolume, 2);
    }

}
