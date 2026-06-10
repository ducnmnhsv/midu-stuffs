package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;

@Data
public class MarketPutThroughDealResponse {
    private String code;
    private String time;
    private String confirmNumber;
    private int matchPrice;
    private long matchVolume;
    private long ptVolume;
    private long ptValue;
    private boolean isCancel;

}
