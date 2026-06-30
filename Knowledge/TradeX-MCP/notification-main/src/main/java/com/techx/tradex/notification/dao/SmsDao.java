package com.techx.tradex.notification.dao;


import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.model.SmsMessageRequest;

public interface SmsDao {
    void sendSms(SmsMessageRequest request, AppConf.SmsServer smsServer) throws Exception;
}
