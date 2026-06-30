package com.techx.tradex.notification.model;

import com.techx.tradex.common.model.notification.Configuration;

import com.techx.tradex.common.model.notification.MethodEnum;
import com.techx.tradex.common.model.requests.DataRequest;

import lombok.Data;

@Data
public class EmailAddReq implements Configuration{
    private String to;
    private String otp;
    private String subject;

    @Override
    public MethodEnum getMethod() {
        // TODO Auto-generated method stub
        return null;
    }
}
