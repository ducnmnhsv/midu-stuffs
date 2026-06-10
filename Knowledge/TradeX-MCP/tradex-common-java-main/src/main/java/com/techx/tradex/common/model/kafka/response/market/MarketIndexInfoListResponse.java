package com.techx.tradex.common.model.kafka.response.market;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.List;

@Data
public class MarketIndexInfoListResponse {
    private String code;
    private String time;
    private double open;
    private double high;
    private double low;
    private double last;
    private double change;
    private double rate;
    private long tradingVolume;
    private long tradingValue;
    @JsonIgnore
    private int highlightNumber;
    private String market;
    private String indexName;
    private String indexNameEn;
    public boolean isHighlight;

    private long priorVolume;
    private double ptVolume;
    private Integer upCount;
    private Integer ceilingCount;
    private Integer downCount;
    private Integer floorCount;
    private Integer unchangedCount;
    private String date;
    private List<MarketIndexInfoResponse.Session> sessions = new ArrayList<>();

    @JsonProperty("isHighlight")
    public boolean isHighlight() {
        return isHighlight;
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

