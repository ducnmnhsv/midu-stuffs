package com.techx.tradex.common.model.kafka.response.market;

import lombok.Data;

@Data
public class MarketListedStockResponse {

    private String code;
    private String market;
    private String securitiesType;
    private String companyName;
    private String companyNameEn;

}

