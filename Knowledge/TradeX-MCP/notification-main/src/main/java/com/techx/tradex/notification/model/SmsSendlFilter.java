package com.techx.tradex.notification.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SmsSendlFilter {
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("sms_from")
    private String smsFrom;

    private Map<String,String> contents;

    @JsonProperty("include_phone_numbers")
    private List<String> phoneNumbers;
}