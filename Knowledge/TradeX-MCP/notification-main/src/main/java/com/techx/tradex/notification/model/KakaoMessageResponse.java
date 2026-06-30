package com.techx.tradex.notification.model;

import lombok.Data;

import java.util.List;

@Data
public class KakaoMessageResponse {
    private String code;
    private String message;
    private List<Result> results;
    private String additionalInformation;

    @Data
    public static class Result {
        private String result;
        private String messageId;
    }
}
