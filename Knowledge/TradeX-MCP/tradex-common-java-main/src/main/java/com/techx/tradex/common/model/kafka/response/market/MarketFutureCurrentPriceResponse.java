package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;

@Data
public class MarketFutureCurrentPriceResponse {
    private String code;
    private double last;
    private double open;
    private double high;
    private double low;
    private double change;
    private double rate;
    private long tradingVolume;
    private long tradingValue;
    private String ceilingFloorEqual;
    private String time;
    private double ceilingPrice;
    private double floorPrice;
    private double referencePrice;
    private double averagePrice;

    private String highTime;
    private String lowTime;
    private double expectedPrice;

}
