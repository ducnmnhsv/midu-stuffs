package com.techx.tradex.notification.controllers;

import com.techx.tradex.common.model.notification.NotificationMessage;

public interface ResponseProcess<T> {
    void response(T body, NotificationMessage notificationMessage, Exception e);
}
