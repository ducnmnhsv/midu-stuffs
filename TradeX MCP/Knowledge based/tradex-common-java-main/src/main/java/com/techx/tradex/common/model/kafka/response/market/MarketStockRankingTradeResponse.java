package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;
import org.apache.commons.math3.util.Precision;

@Data
public class MarketStockRankingTradeResponse {
    private String code;
    private int last;
    private int change;
    private double rate;
    private long tradingVolume;
    private long tradingValue;
    private double turnoverRate;

    public void setTurnoverRate(double turnoverRate) {
        this.turnoverRate = Precision.round(turnoverRate, 2);
    }
}
