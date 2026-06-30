package com.difisoft.nhsv.admin.domain.request;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoricalPortfolioRequest {
    private Long marketLeaderId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private Date fromDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private Date toDate;
    private Boolean sortAsc;
    private Integer pageNumber;
    private Integer pageSize;
}