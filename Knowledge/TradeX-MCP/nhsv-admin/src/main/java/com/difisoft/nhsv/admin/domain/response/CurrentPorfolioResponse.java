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
public class CurrentPorfolioResponse {
    private String uploadedDateTime;
    private List<CurrentPorfolio> currentPortfolio;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CurrentPorfolio {
        private String stockCode;
        private Double stockWeight;
    }
}
