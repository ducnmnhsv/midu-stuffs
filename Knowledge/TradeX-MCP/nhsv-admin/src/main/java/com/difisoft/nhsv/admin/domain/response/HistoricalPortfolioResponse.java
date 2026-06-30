package com.difisoft.nhsv.admin.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoricalPortfolioResponse {
    private String uploadedDateTime;
    private Long portfolioId;
}