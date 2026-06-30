package com.difisoft.nhsv.admin.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoricalPortfolioAllStocksRequest {
    private Long portfolioId;
    private Integer pageNumber;
    private Integer pageSize;

    public String objToString() {
        return MessageFormat.format("{0}_{1}_{2}", portfolioId, pageNumber, pageSize);
    }
}
