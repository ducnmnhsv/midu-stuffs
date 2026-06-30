package com.techx.tradex.notification.model;

import lombok.Data;

@Data
public class SmsMessageRequest {
    private String phoneNumber;
    private String content;
}

