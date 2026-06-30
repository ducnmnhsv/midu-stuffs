package com.difisoft.nhsv.admin.domain.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PortfolioUploadRequest {
    @NotNull
    private Long mlUserId;
    private List<PortfolioUploadItem> items;

    @Data
    public static class PortfolioUploadItem {
        @NotBlank
        private String symbol;
        @NotNull
        private Double weight;
    }
}
