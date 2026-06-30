package com.difisoft.nhsv.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app")
@Component
public class AppConf {
    private Redis redis;
    private Boolean isEnableApiResponseDefault;
    private SmsServer smsServer;
    private VietStock vietStock;

    @Data
    public static class Redis {
        private KeyPattern keyPattern;
        private Timeout timeout;
    }

    @Data
    public static class KeyPattern {
        private String cacheSubscriberGrowthRate;
        private String cacheDailyProfitLoss;
    }

    @Data
    public static class Timeout {
        private Long oneDayMilliseconds;
        private Long fifteenMilliseconds;
    }

    @Data
    public static class SmsServer {
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
    public static class VietStock {
        private String host;
        private VietStockEvent event;
        private Integer dayStep = 7;
        private Map<String, String> headers;
    }

    @Data
    public static class VietStockEvent {
        private String url;
        private Map<String, Map<String, String>> input;
    }
}
