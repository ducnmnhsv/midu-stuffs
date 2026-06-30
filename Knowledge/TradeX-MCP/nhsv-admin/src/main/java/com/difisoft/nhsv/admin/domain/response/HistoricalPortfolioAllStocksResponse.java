package com.difisoft.nhsv.admin.domain.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoricalPortfolioAllStocksResponse {
    private Long marketLeaderId;
    private Long portfolioId;
    private List<AllStockCode> allStockCodes;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AllStockCode {
        private String stockCode;
        private Double stockWeight;
    }
}
