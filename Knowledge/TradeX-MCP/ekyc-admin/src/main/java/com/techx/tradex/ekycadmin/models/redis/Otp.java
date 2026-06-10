package com.techx.tradex.ekycadmin.models.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Otp {
    private String id;
    private String otp;
    private String otpTxType;
    private String otpIdType;
    private String username;
}
