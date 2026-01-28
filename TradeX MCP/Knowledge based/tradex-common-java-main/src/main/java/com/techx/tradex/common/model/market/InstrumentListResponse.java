package com.techx.tradex.common.model.market;

import lombok.Data;

import java.util.ArrayList;

public class InstrumentListResponse extends ArrayList<InstrumentListResponse.InstrumentItem> {

    @Data
    public static class InstrumentItem {
        private String instrumentCode;
        private String cfiCode;
        private String currency;
        private String securityExchange;
        private String securityDescription;
        private Integer roundLot;
        private Integer minTradeVolume;
        private Integer contractMultiplier;
        private String maturityMonthYear;
        private String maturityDate;
    }
}
