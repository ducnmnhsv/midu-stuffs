package com.techx.tradex.ekycadmin.models.enums;

public enum LotteLangCode {
    vi("V"),
    en("E"),
    ko("K");

    private final String code;

    LotteLangCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
