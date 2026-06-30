package com.techx.tradex.notification.model;

import com.techx.tradex.common.model.requests.DataRequest;

import lombok.Data;

@Data
public class PhoneNumberAddReq extends DataRequest {
    private String phoneNumber;
}
