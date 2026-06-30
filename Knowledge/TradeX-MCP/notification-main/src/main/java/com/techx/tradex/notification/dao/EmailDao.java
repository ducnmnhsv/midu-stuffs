package com.techx.tradex.notification.dao;


import com.techx.tradex.notification.model.EmailMessageRequest;

public interface EmailDao {
    void sendMessage(EmailMessageRequest request) throws Exception;
}
