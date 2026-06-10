package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.List;

@Data
public class MarketStockInfoResponse {
    private double last;
    private double open;
    private double high;
    private double low;
    private double change;
    private double rate;
    private long tradingVolume;
    private double tradingValue;
    private String ceilingFloorEqual;
    private String code;
    private String companyName;
    private String companyNameEn;
    private String market;
    private String industry;
    private String time;
    private int ceilingPrice;
    private int floorPrice;
    private int referencePrice;
    private int averagePrice;
    private long normalVolume;
    private long normalValue;
    private long ptVolume;
    private double ptValue;
    private long priorVolume;
    private double turnoverRate;
    private double parValue;
    private long listedQuantity;
    private long foreignerTotalRoom;
    private long foreignerCurrentRoom;
    private long foreignerBuyVolume;
    private long foreignerSellVolume;

    private List<PriceItem> bidOfferList = new ArrayList<>();

    private long totalOfferVolume;
    private long totalOfferCount;
    private long totalBidVolume;
    private long totalBidCount;
    private String rights;

    private double highPrice52Weeks;
    private double lowPrice52Weeks;

    @Data
    public static class PriceItem {
        private int bidPrice;
        private int offerPrice;
        private int bidVolume;
        private int offerVolume;
        private int bidVolumeChange;
        private int offerVolumeChange;
    }

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

    public void setPtValue(double ptValue) {
        this.ptValue = Precision.round(ptValue, 2);
    }

    public void setParValue(double parValue) {
        this.parValue = Precision.round(parValue, 2);
    }

    public void setHighPrice52Weeks(double highPrice52Weeks) {
        this.highPrice52Weeks = Precision.round(highPrice52Weeks, 2);
    }

    public void setLowPrice52Weeks(double lowPrice52Weeks) {
        this.lowPrice52Weeks = Precision.round(lowPrice52Weeks, 2);
    }

    public void setRate(double rate) {
        this.rate = Precision.round(rate, 2);
    }

    public void setTurnoverRate(double turnoverRate) {
        this.turnoverRate = Precision.round(turnoverRate, 2);
    }

}
