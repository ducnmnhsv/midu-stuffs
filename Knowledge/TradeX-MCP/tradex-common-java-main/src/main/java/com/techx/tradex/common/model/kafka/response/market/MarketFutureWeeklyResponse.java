package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;

import java.util.ArrayList;

@Data
public class MarketFutureWeeklyResponse extends ArrayList<MarketFutureWeeklyResponse.MarketFutureWeeklyItem> {
    private String next = null;
    private int offSet = 0;

    @Data
    public static class MarketFutureWeeklyItem {
        private String date;
        private double price;
        private double change;
        private double rate;
        private long volume;
        private long value;
        private double open;
        private double high;
        private double mBasis;
        private double tBasis;
        private double theoryPrice;
        private double index;
        private long openInterest;
    }

}
