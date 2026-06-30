package com.techx.tradex.notification.model;

import lombok.Data;

import java.util.List;

@Data
public class EmailMessageRequest {
    private List<String> recipients;
    private List<String> ccList;
    private List<String> bccList;
    private String from;
    private String subject;
    private String htmlContent;
    private String plainTextContent;
}

