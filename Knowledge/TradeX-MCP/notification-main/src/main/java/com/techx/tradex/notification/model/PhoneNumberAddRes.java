package com.techx.tradex.notification.model;

import lombok.Data;

@Data
public class PhoneNumberAddRes {
    private String phoneNumber;
    
    public static PhoneNumberAddRes fromNotification(String phoneNumber) {
        PhoneNumberAddRes res = new PhoneNumberAddRes();
        res.setPhoneNumber(phoneNumber);
        return res;
    }
}
