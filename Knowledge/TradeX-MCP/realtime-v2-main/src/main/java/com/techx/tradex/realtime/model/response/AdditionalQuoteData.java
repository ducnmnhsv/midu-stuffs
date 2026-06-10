package com.techx.tradex.realtime.model.response;

import lombok.Data;

@Data
public class AdditionalQuoteData {
    private String code;
    private Integer tc = 0; // trade count
    private Integer utc = 0; // unTrade count
}
