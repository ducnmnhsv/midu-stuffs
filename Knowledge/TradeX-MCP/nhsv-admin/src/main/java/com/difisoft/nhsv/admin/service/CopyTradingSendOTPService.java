package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.nhsv.admin.domain.request.CopyTradingSendOTPRequest;
import com.difisoft.nhsv.admin.domain.request.CopyTradingVerifyOTPRequest;
import com.difisoft.nhsv.admin.domain.response.CopyTradingSendOTPResponse;
import com.difisoft.nhsv.admin.domain.response.CopyTradingVerifyOTPResponse;

public interface CopyTradingSendOTPService {
    CopyTradingSendOTPResponse generateOtp(CopyTradingSendOTPRequest request, RequestContext<CopyTradingSendOTPRequest> ctx);

    CopyTradingVerifyOTPResponse verifyOtp(CopyTradingVerifyOTPRequest request, RequestContext<CopyTradingVerifyOTPRequest> ctx);
}
