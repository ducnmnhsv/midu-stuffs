package com.techx.tradex.common.constants;

public enum FuturesTypeEnum {

    HNX("15", "HNX");

    private String exchange;
    private String description;

    FuturesTypeEnum(String exchange, String description) {
        this.exchange = exchange;
        this.description = description;
    }

    public String getExchange() {
        return exchange;
    }

    public String getDescription() {
        return description;
    }
}
