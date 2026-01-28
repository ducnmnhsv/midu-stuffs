package com.techx.tradex.common.constants;

public enum StockSecuritiesTypeEnum {

    ALL("ALL"), STOCK("STOCK"), ETF("ETF"), FUND("FUND");

    private String value;

    StockSecuritiesTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
