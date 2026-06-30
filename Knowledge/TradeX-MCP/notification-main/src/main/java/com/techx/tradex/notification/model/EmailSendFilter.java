package com.techx.tradex.notification.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EmailSendFilter {
    @JsonProperty("app_id")
    private String appId;
    
    @JsonProperty("include_email_tokens")
    private List<String> to;
    
    @JsonProperty("email_subject")
    private String subject;
    
    @JsonProperty("email_body")
    private String body;
    
    @JsonProperty("email_from_name")
    private String fromName;
}
