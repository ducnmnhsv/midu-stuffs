package com.techx.tradex.ekycadmin.models.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyOtpResponse {
    String otpKey;
    String expiredTime;
}
