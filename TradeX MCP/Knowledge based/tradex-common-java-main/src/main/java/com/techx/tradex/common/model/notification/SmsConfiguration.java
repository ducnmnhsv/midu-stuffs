package com.techx.tradex.common.model.notification;

import lombok.Data;

@Data
public class SmsConfiguration implements Configuration {
    private String phoneNumber;

    @Override
    public MethodEnum getMethod() {
        return MethodEnum.SMS;
    }
}
