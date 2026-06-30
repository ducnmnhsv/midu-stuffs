package com.difisoft.nhsv.admin.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CopyTradingVerifyOTPRequest {
    private String otpKey;
    private String otpValue;
}
