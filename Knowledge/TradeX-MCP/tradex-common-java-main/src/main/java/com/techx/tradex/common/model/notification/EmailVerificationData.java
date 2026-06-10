package com.techx.tradex.common.model.notification;

import lombok.Data;

@Data
public class EmailVerificationData implements TemplateData {
    private String activationCode;
    private String expirationTime;
    private String username;
    private String baseUrl;

    @Override
    public String getTemplate() {
        return "email_verify";
    }
}
