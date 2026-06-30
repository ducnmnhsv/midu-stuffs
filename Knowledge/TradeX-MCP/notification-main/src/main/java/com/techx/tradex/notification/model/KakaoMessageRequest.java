package com.techx.tradex.notification.model;

import lombok.Data;

import java.util.List;

@Data
public class KakaoMessageRequest {
    private String usercode;
    private String deptcode;
    private String yellowidKey;
    private List<Message> messages;

    @Data
    public static class Message {
        private String messageId;
        private String to;
        private String text;
        private String from;
        private String templateCode;
        private String reservedTime;
        private String reSend; // Y|N
        private String reText;
        private List<Button> buttons;
    }

    @Data
    public static class Button {
        private String buttonType;
        private String buttonName;
        private String buttonUrl;
    }
}

