package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;

@Data
public class MarketStockQuoteResponse {
    private String code;
    private String time;
    private int last;
    private int open;
    private int high;
    private int low;
    private int change;
    private double rate;
    private long tradingVolume;
    private long matchingVolume;
    private String matchedBy;
    private String ceilingFloorEqual;
    private int sequence;
}
