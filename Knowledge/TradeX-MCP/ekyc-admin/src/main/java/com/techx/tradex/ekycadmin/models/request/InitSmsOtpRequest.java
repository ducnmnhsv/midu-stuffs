package com.techx.tradex.ekycadmin.models.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class InitSmsOtpRequest {

    private String method;
    private Template template;
    private String locale;
    private String configuration;
    private String domain;
    private String type;

    @Data
    static class Template {
        private Sms ekyc_sms_otp;
    }

    @Data
    static class Sms {
        private String otp;
    }

    @Data
    static class Configuration {
        private String domain;
        private String phoneNumber;
    }

    public InitSmsOtpRequest toInitSmsOtpRequest(InitSmsOtpRequest request, String otp, String phoneNumber, ObjectMapper objectMapper)
        throws JsonProcessingException {
        Sms sms = new Sms();
        sms.setOtp(otp);
        Template template = new Template();
        template.setEkyc_sms_otp(sms);
        Configuration configuration = new Configuration();
        configuration.setDomain(request.getDomain());
        configuration.setPhoneNumber(phoneNumber);
        request.setLocale(request.getLocale() == null ? "vi" : request.getLocale());
        request.setConfiguration(objectMapper.writeValueAsString(configuration));
        request.setTemplate(template);
        return request;
    }
}
