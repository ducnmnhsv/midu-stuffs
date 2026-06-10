package com.techx.tradex.common.model.kafka.response.market;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class MarketIndexQuoteMinuteResponse {
    private String code;
    private String time;
    @JsonIgnore
    private String actualTime;
    @JsonIgnore
    private Date dateTime;
    private int last;
    private int open;
    private int high;
    private int low;
    private long tradingVolume;
    private long tradingValue;
    private int lastValue;
    private long periodTradingVolume;

    public void setPeriodTradingVolume(long periodTradingVolume) {
        this.periodTradingVolume = Math.abs(periodTradingVolume);
    }
}
