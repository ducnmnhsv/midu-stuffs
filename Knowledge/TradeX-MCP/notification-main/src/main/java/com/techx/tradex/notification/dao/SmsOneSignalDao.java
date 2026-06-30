package com.techx.tradex.notification.dao;

import com.techx.tradex.notification.model.SmsOneSignalRequest;

public interface SmsOneSignalDao {
    void sendSms(SmsOneSignalRequest request) throws Exception;

    void registerPhoneNumber(SmsOneSignalRequest request);
}
