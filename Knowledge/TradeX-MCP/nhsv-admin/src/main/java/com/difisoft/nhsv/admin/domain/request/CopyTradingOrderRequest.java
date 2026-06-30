package com.difisoft.nhsv.admin.domain.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class CopyTradingOrderRequest {
    private Long copyPortfolioId;
    @NotBlank
    private Long subScriberId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String stockCode;
    private String sellBuyType;
    private int page = 0;
    private int size = 20;
}
