package com.techx.tradex.notification.dao;


import com.techx.tradex.common.model.notification.NotificationMessage;
import com.vng.zalo.sdk.APIException;

public interface ZaloDao {
    void sendMessage(NotificationMessage notificationMessage, String toList) throws Exception;

    void sendMessageById(NotificationMessage notificationMessage, String userId, int retry) throws APIException;
}
