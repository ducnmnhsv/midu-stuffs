package com.techx.tradex.common.constants;

public enum UDFHistoryStatusEnum {
    OK("ok"), NO_DATE("no_data");

    private String status;

    UDFHistoryStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
