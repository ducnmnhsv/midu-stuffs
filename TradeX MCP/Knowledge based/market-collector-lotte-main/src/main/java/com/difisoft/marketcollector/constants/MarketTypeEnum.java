package com.difisoft.marketcollector.constants;

public enum MarketTypeEnum {

    ALL("", ""), HOSE("01", "HOSE"), UPCOM("31", "UPCOM"), HNX("11", "HNX"), ETF_HOSE("04", "ETF"), FUND_HOSE("03", "FUND");

    private String exchange;
    private String description;

    MarketTypeEnum(String exchange, String description) {
        this.exchange = exchange;
        this.description = description;
    }

    public static MarketTypeEnum from(String exchange) {
        if (exchange.equalsIgnoreCase(HOSE.exchange)) {
            return HOSE;
        }
        if (exchange.equalsIgnoreCase(UPCOM.exchange)) {
            return UPCOM;
        }
        if (exchange.equalsIgnoreCase(HNX.exchange)) {
            return HNX;
        }
        if (exchange.equalsIgnoreCase(ETF_HOSE.exchange)) {
            return ETF_HOSE;
        }
        if (exchange.equalsIgnoreCase(FUND_HOSE.exchange)) {
            return FUND_HOSE;
        }
        return null;
    }

    public String getExchange() {
        return exchange;
    }

    public String getDescription() {
        return description;
    }
}
