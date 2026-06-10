package com.techx.tradex.common.model.responses;

import lombok.Data;

@Data
public class SessionResponse {

    private double last;
    private double change;
    private double rate;
    private Integer tradingVolume;
    private double tradingValue;

}
