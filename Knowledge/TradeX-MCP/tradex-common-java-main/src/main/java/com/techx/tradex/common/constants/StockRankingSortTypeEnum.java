package com.techx.tradex.common.constants;

public enum StockRankingSortTypeEnum {

    TURNOVER_RATE("turnoverRate"), TRADING_VOLUME("tradingVolume"), TRADING_VALUE("tradingValue");

    private String field;

    StockRankingSortTypeEnum(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
