package com.techx.tradex.notification.dao;

import com.techx.tradex.notification.model.SmsOneSignalRequest;

public interface OneSignalDao {
   void sendRequestOneSignal(SmsOneSignalRequest request);
}
