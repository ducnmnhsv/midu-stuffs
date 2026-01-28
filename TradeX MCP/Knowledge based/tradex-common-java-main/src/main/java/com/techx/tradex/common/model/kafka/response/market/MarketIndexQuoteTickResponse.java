package com.techx.tradex.common.model.kafka.response.market;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class MarketIndexQuoteTickResponse {

    private String code;
    private String time;
    private int last;
    private int open;
    private int high;
    private int low;
    private long tradingVolume;
    private long tradingValue;
    private int lastValue;
    private long periodTradingVolume;

    @JsonIgnore
    private int mod;
    @JsonIgnore
    Date dateTime;

    public void setPeriodTradingVolume(long periodTradingVolume) {
        this.periodTradingVolume = Math.abs(periodTradingVolume);
    }
}
