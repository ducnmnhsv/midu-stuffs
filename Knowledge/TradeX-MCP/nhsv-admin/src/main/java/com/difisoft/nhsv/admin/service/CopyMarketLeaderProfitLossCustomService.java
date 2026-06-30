package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.nhsv.admin.domain.request.GetAllMarketLeaderRequest;
import com.difisoft.nhsv.admin.domain.request.MarketLeaderPeriodProfitLossRequest;
import com.difisoft.nhsv.admin.domain.request.MarketLeaderProfitLossRequest;
import com.difisoft.nhsv.admin.domain.request.RecalculateProfitLossByPeriodRequest;
import com.difisoft.nhsv.admin.domain.response.GenericResponse;
import com.difisoft.nhsv.admin.domain.response.GetAllMarketLeaderResponse;
import com.difisoft.nhsv.admin.domain.response.MarketLeaderPeriodProfitLossResponse;
import com.difisoft.nhsv.admin.domain.response.MarketLeaderProfitLossResponse;

import java.util.List;

public interface CopyMarketLeaderProfitLossCustomService {
    GenericResponse<List<MarketLeaderProfitLossResponse>> findAllMarketLeaderDailyProfitLoss(MarketLeaderProfitLossRequest request, RequestContext<MarketLeaderProfitLossRequest> ctx);

    GenericResponse<List<MarketLeaderPeriodProfitLossResponse>> findMarketLeaderProfitLossByPeriod(MarketLeaderPeriodProfitLossRequest request, RequestContext<MarketLeaderPeriodProfitLossRequest> ctx);

    void dailyProfitLossJob();

    GenericResponse<String> recalculateProfitLossByPeriod(RecalculateProfitLossByPeriodRequest request, RequestContext<RecalculateProfitLossByPeriodRequest> ctx);

    GenericResponse<List<GetAllMarketLeaderResponse>> getAllMarketLeader(GetAllMarketLeaderRequest request, RequestContext<GetAllMarketLeaderRequest> ctx);
}
