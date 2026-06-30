package com.difisoft.nhsv.admin.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketHistoryJobResultResponse {
    private Long id;
    private Boolean isSuccess;
    private ZonedDateTime timeStart;
    private ZonedDateTime timeEnd;
    private String error;
    private String symbols;
    private Long userId;
}
