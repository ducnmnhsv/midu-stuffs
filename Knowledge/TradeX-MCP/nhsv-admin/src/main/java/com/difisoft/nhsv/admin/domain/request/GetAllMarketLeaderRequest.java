package com.difisoft.nhsv.admin.domain.request;

import lombok.Data;

@Data
public class GetAllMarketLeaderRequest {
    private String category = "PROFIT_RATE";
    private String search;
    private String period = "ALL";
    private int pageNumber = 0;
    private int pageSize = 20;
}
