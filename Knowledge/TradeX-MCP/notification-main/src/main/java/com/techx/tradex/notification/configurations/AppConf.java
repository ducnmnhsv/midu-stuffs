package com.techx.tradex.notification.configurations;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app")
@Data
public class AppConf {
    private Kakao kakao;
    private Email email;
    private Zalo zalo;
    private Template template;
    private OneSignal oneSignal;
    private SocketClusterConf socketCluster;
    private Map<String, Map<String, String>> templatesMap = new HashMap<>();
    private Map<String, OneSignalApp> oneSignalMap = new HashMap<>();
    private Map<String, SmsServer> smsServerMap = new HashMap<>();
    private String domain;
    private String kafkaUrl;
    private String clusterId;
    private String tradexOnlyRequestHandler;
    private String nodeId;
    private Topics topics;
    private FptSms fptSms;
    private SmsOneSignal SmsOneSignal;
    private String notification = "paave-notification";
    private String notificationUri = "internal:/api/v1/notification/updateNotification";
    private Retry retry;

    @Data
    public static class Retry {
        private Integer maxAttempts;
        private Integer maxDelay;
    }

    @Data
    public static class SmsOneSignal {
        private String phoneFrom;
        private String phoneTo;
    }

    @Data
    public static class FptSms {
        private String authUrl;
        private String sendUrl;
        private String clientId;
        private String clientSecret;
        private String scope;
        private String brandName;
    }

    @Data
    public static class Topics {
        private String requestResponseListener;
        private String requestListener;
        private String paaveNotification;
    }

    @Data
    public static class Zalo {
        private String accessToken;
        private String sendMessageUrl;
    }

    @Data
    public static class Kakao {
        private String url;
        private String companyCode;
        private String id;
        private String password;
        private String yellowIdKey;
        private Map<String, String> templateCodes;
    }

    @Data
    public static class OneSignalApp {
        private String appId;
        private String apiKey;
    }

    @Data
    public static class SmsServer {
        private String domain;
        private String url;
        private String type;
        private String user;
        private String pass;
        private String senderName;
        private Boolean isUnicode;
        private Boolean isFlash;
        private String soapAction;
    }

    @Data
    public static class Email {
        private String endpoint;
        private int port;
        private String username;
        private String smtpUsername;
        private String smtpPassword;
        private String sender;
        private String support;
        private String subject;
    }

    @Data
    public static class Template {
        private String dir;
    }

    @Data
    public static class OneSignal {
        private String appId;
        private String apiKey;
    }

    @Data
    public static class SocketClusterConf {
        public static final String CODEC_MIN_BIN = "codecMinBin";
        private String hostname = "localhost";
        private int port = 8000;
        private String path = "socketcluster/";
        private String codec = CODEC_MIN_BIN;
        private boolean secure = false;
        private boolean autoReconnection = true;
        private boolean logMessage = false;
        private boolean usingKafka = false;
        private String kafkaTopic = "ws.broadcast";
    }

    public String getKafkaBootstraps() {
        return this.kafkaUrl.replace(";", ",");
    }
}
