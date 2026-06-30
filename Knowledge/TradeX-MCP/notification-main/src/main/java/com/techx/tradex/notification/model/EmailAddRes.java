package com.techx.tradex.notification.model;

import lombok.Data;

@Data
public class EmailAddRes {
    private String email;
    
    public static EmailAddRes fromNotification(String email) {
        EmailAddRes res = new EmailAddRes();
        res.setEmail(email);
        return res;
    } 
}
