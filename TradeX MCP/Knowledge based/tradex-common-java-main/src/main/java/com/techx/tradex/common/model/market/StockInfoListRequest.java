package com.techx.tradex.common.model.market;

import lombok.Data;

import java.util.List;

@Data
public class StockInfoListRequest {
    private List<String> stockList;
    private String stockCode;
    private Integer fetchCount;
}
