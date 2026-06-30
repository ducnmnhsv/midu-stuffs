package com.techx.tradex.notification.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PhoneNumberRegisterFilter {
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("device_type")
    private String deviceType;

    private String identifier;

    @JsonProperty("external_user_id")
    private String userId;
}