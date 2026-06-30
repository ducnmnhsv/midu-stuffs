package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.nhsv.admin.domain.request.CopyTradingSubscriberEstPortfolioValueRequest;
import com.difisoft.nhsv.admin.domain.response.CopyTradingSubscriberEstPortfolioValueResponse;

public interface CopyTradingSubscriberEstPortfolioValueService {

    CopyTradingSubscriberEstPortfolioValueResponse getEstPortfolioValue(
        CopyTradingSubscriberEstPortfolioValueRequest request, RequestContext<CopyTradingSubscriberEstPortfolioValueRequest> ctx);
}
