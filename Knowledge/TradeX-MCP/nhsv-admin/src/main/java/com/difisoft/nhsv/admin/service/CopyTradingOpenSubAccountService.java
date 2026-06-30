package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.nhsv.admin.domain.request.CopyTradingCheckSubStatusRequest;
import com.difisoft.nhsv.admin.domain.request.CopyTradingOpenSubAccountRequest;
import com.difisoft.nhsv.admin.domain.response.CopyTradingCheckSubStatusResponse;
import com.difisoft.nhsv.admin.domain.response.CopyTradingOpenSubAccountResponse;

public interface CopyTradingOpenSubAccountService {

    CopyTradingOpenSubAccountResponse openSubAccount(CopyTradingOpenSubAccountRequest request,
                                                     RequestContext<CopyTradingOpenSubAccountRequest> ctx);

    CopyTradingCheckSubStatusResponse checkSubStatus(CopyTradingCheckSubStatusRequest request,
                                                     RequestContext<CopyTradingCheckSubStatusRequest> ctx);

    void updateStatusJob();
}
