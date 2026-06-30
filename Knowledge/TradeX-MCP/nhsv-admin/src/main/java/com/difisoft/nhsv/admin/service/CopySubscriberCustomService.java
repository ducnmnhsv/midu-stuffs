package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.model.responses.MessageResponse;
import com.difisoft.nhsv.admin.domain.request.MarketLeaderSubGrowthRateRequest;
import com.difisoft.nhsv.admin.domain.request.SubscribeRequest;
import com.difisoft.nhsv.admin.domain.request.SubscriberInformationRequest;
import com.difisoft.nhsv.admin.domain.request.UnSubscribeRequest;
import com.difisoft.nhsv.admin.domain.response.GenericResponse;
import com.difisoft.nhsv.admin.domain.response.MarketLeaderSubGrowthRateResponse;
import com.difisoft.nhsv.admin.domain.response.SubscriberInformationResponse;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public interface CopySubscriberCustomService extends CopySubscriberService {
    Page<CopySubscriberDTO> findAllByMlId(Long mlUserId, Pageable pageable);

    MessageResponse subscribe(SubscribeRequest request, RequestContext<SubscribeRequest> ctx);

    MessageResponse unSubscribe(UnSubscribeRequest request, RequestContext<UnSubscribeRequest> ctx);

    GenericResponse<List<SubscriberInformationResponse>> findSubscriberInformation(SubscriberInformationRequest request,
            RequestContext<SubscriberInformationRequest> ctx);

    GenericResponse<MarketLeaderSubGrowthRateResponse> findMarketLeaderSubscriberGrowthRate(MarketLeaderSubGrowthRateRequest request, RequestContext<MarketLeaderSubGrowthRateRequest> ctx);
}
