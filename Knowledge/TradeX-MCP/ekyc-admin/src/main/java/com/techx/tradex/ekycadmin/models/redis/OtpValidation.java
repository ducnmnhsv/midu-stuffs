package com.techx.tradex.ekycadmin.models.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpValidation {
    private String username;
    private Integer count;
    private Integer failedCount;
    private LocalDateTime latestRequest;
}
