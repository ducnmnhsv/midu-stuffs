package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.domain.request.HistoricalPortfolioRequest;
import com.difisoft.nhsv.admin.domain.request.CurrentPorfolioRequest;
import com.difisoft.nhsv.admin.domain.request.HistoricalPortfolioAllStocksRequest;
import com.difisoft.nhsv.admin.domain.request.MarketLeaderProfileRequest;
import com.difisoft.nhsv.admin.domain.request.MtsMarketLeadersRequest;
import com.difisoft.nhsv.admin.domain.response.CurrentPorfolioResponse;
import com.difisoft.nhsv.admin.domain.response.GenericResponse;
import com.difisoft.nhsv.admin.domain.response.HistoricalPortfolioAllStocksResponse;
import com.difisoft.nhsv.admin.domain.response.HistoricalPortfolioResponse;
import com.difisoft.nhsv.admin.domain.response.MarketLeaderProfileResponse;
import com.difisoft.nhsv.admin.domain.response.MtsMarketLeadersResponse;
import com.difisoft.nhsv.admin.service.dto.AdminUserDTO;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CopyUserService {
    Optional<User> findById(Long mlUserId);

    GenericResponse<List<MtsMarketLeadersResponse>> findAllMarketLeader(MtsMarketLeadersRequest request, RequestContext<MtsMarketLeadersRequest> ctx);

    GenericResponse<MarketLeaderProfileResponse> findMarketLeaderProfile(MarketLeaderProfileRequest request, RequestContext<MarketLeaderProfileRequest> ctx);
   
    GenericResponse<List<HistoricalPortfolioResponse>> findHistoricalPortfolio(HistoricalPortfolioRequest request, RequestContext<HistoricalPortfolioRequest> ctx);

    GenericResponse<CurrentPorfolioResponse> findCurrentPortfolio(CurrentPorfolioRequest request, RequestContext<CurrentPorfolioRequest> ctx);

    GenericResponse<HistoricalPortfolioAllStocksResponse> findHistoricalPortfolioAllStocks(HistoricalPortfolioAllStocksRequest request, RequestContext<HistoricalPortfolioAllStocksRequest> ctx);

    AdminUserDTO findMLAccountInfo(Long userId);

    List<User> findAllUserByAuthorityTypeAndStatus(@Param("authName") String authName, @Param("activated") boolean activated);

    List<User> findAllByIdsAndTypeAndStatus(List<Long> mlUserIds, String authority, Boolean status);

    List<User> findAll();
}
