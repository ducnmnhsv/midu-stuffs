package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;
import org.apache.commons.math3.util.Precision;

@Data
public class MarketPutThroughAdvertiseResponse {
    private String code;
    private String time;
    private String secId;
    private String traderId;
    private String sellBuyType;
    private double price;
    private long quantity;
    private long ptVolume;
    private long ptValue;
    private String contact;
    private boolean isCancel;

    public void setPrice(double price) {
        this.price = Precision.round(price, 2);
    }
}
