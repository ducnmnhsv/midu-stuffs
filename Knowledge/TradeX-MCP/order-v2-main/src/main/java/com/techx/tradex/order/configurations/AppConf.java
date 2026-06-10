package com.techx.tradex.order.configurations;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
@Data
public class AppConf {
    private String clusterId;
    private String nodeId;
    private int noThreadHandler = 5;
    private int maxThread = 30;
    private String kafkaUrl;
    private Topics topics;
    private Uri uri;
    private String domain;
    private String core;
    private boolean isAccountCaseSensitive = false;
    private boolean enablePlaceOrderAfterMarketClose;
    private boolean enableLotteBridge = false;
    private Mode mode = Mode.ENGINE_AND_QUERY;

    @Data
    public static class Topics {
        private String market;
        private String masRestBridge;
        private String fssRestBridge;
        private String tuxedo;
        private String quoteUpdate;
        private String orderMatch;
        private String notification;
        private String requestResponseListener;
        private String requestListener;
        private String updateConditionalOrder;
        private String lotteBridge;
    }

    @Data
    public static class Uri {
        private String symbolLatest;
        private String tuxEquityOrder;
        private String tuxDrOrder;
        private String masEqtPlaceOrder;
        private String masDrPlaceOrder;
        private String masEqtCancelOrder;
        private String masDrCancelOrder;
        private String lotteEquityOrder;

    }

    public String getKafkaBootstraps() {
        return this.kafkaUrl.replace(";", ",");
    }

    public enum Mode {
        ENGINE,
        QUERY,
        ENGINE_AND_QUERY;
    }
}
