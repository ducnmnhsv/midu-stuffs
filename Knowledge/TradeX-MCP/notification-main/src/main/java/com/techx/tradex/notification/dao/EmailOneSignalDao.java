package com.techx.tradex.notification.dao;

import com.techx.tradex.notification.model.SmsOneSignalRequest;

public interface EmailOneSignalDao {

    void sendEmail(SmsOneSignalRequest request) throws Exception;
}
