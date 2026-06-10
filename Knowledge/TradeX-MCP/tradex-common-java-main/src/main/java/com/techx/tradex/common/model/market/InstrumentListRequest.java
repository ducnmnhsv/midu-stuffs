package com.techx.tradex.common.model.market;

import lombok.Data;

@Data
public class InstrumentListRequest {
    private String instrumentCode;
    private int fetchCount = 20;
}
