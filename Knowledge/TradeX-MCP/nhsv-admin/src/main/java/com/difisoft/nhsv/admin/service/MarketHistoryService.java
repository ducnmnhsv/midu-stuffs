package com.difisoft.nhsv.admin.service;


import com.difisoft.nhsv.admin.domain.request.MarketHistoryRequest;
import com.difisoft.nhsv.admin.domain.response.MarketHistoryJobResultResponse;
import com.difisoft.nhsv.admin.domain.response.primary.MarketHistoryJobResultPrimaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface MarketHistoryService {

    void uploadMarketHistory(MarketHistoryRequest request);

    MarketHistoryJobResultResponse getLatestJobResult();

    Page<MarketHistoryJobResultPrimaryResponse> getJobResult(Pageable pageable);
}
