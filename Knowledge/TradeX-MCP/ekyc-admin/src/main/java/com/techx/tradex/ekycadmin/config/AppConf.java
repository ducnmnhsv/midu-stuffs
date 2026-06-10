package com.techx.tradex.ekycadmin.config;

import com.techx.tradex.ekycadmin.config.defaultConfig.Datas;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Data
@ConfigurationProperties(prefix = "app")
public class AppConf {

    private String clusterId;
    private String nodeId;
    private String kafkaUrl;
    private Topics topics;
    private String domain;
    private Redis redis;
    private Integer otpLength;
    private Integer otpGenTime;
    private Long otpKeyLifeTime;
    private Integer otpMaxGenTime;
    private OtpLifeTime otpLifeTime;
    private Integer defaulltIdCardExpiredTime;
    private boolean enableCallTllOpenAccount;
    private Double matchThresholdPercentToCallTllOpenAccount = 90.0;
    private TTLConfig ttlConfig;
    private boolean enableDebugPlaceIssueMapping = false;
    private boolean enableRequireGender = false;
    private String addressRegex = "^[a-zA-Z0-9\\u0080-\\u9fff-/\\?:\\(\\)\\.,'+ ]{15,}$";
    private boolean enableAddSignatureToCore = true;
    private ResizeSignature resizeSignature;
    private LotteConfig lotteConfig;
    private String core;
    private Boolean callCoreNoAsync;
    private Boolean checkMatchingRate;
    private Integer maxBankList;
    private Integer maxPublicCoop;
    private Integer maxBlockholder;
    private String keyPath = "src/main/resources/";
    private VNPT vnpt;
    private boolean defaultFileName = true;
    private Difisoft difisoft;
    private int maxRetryCallTllGetContract = 10;
    private Integer optMaxVerifyFail;
    private Boolean handlerVerifyOtpFail;
    private Map<String, Double> matchThresholdPercent = new HashMap<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Pattern addressRegexPattern = null;

    private FeignClient feignClient;
    private TemplateEContract templateEContract;
    private ThreadPoolConfig threadPool;
    private Cron cron;

    private FileStorage fileStorage;

    @Data
    public static class Cron {

        private String eKycUpdateAccNumJob;
        private boolean eKycUpdateAccNumJobActiveStatus;
        private String initiateFptEContractJob;
        private boolean initiateFptEContractJobJobActiveStatus;
        private Long eKycUpdateAccNumJobTimeIntervalMilliseconds;
        private Long initiateFptEContractJobIntervalMilliseconds;
    }

    @Data
    public static class ThreadPoolConfig {

        private String threadNamePrefixSet;
        private Integer corePoolSize;
        private Integer queueCapacity;
        private Integer maxPoolSize;
        private Integer keepAliveSeconds;
        private Integer awaitTerminationSeconds;
        private Boolean waitForTasksToCompleteOnShutdown;
        private String schedulerThreadNamePrefixSet;
        private Integer schedulerPoolSize;
        private Integer schedulerAwaitTerminationSeconds;
        private Boolean schedulerWaitForTasksToCompleteOnShutdown;
        private Long maxPeriodQuerySeconds;
        private Long firstPeriodTimeSecond;
        private Long firstPeriodDelayTimeMillisecond;
        private Long last29MinutestPeriodDelayMillisecond;
    }

    @Data
    public static class TemplateEContract {

        private DefaultFields defaultFields;
    }

    @Data
    public static class DefaultFields {

        private HDMTK hdmtk;
    }

    @Data
    public static class HDMTK {

        private String attrs;
        private String selector;
        private String payload;
        private String recipientId;
        private String country;
        private String type;
        private String photoFrontSideIDCardContentType;
        private String photoBackSideIDCardContentType;
        private String statusCode;
        private String passportID;
        private String resourceType;
        private String refId;
        private String photoIDCard;
        private String photoIDCardContentType;
        private String alias;
        private String syncType;
        private Datas datas;
    }

    @Data
    public static class FeignClient {

        private LotteApi lotteApi;
        private Fpt fpt;
    }

    @Data
    public static class Fpt {

        private EContract eContract;
    }

    @Data
    public static class EContract {

        private String name;
        private String host;
        private LoginInfo loginInfo;
        private Template template;
    }

    @Data
    public static class Template {

        private Alias alias;
    }

    @Data
    public static class Alias {

        private String hdmtk;
    }

    @Data
    public static class LoginInfo {

        private String username;
        private String password;
        private String clientId;
        private String clientSecret;
    }

    @Data
    public static class LotteApi {

        private String name;
        private String host;
        private String apiKey;
        private Long timeoutMinutes;
        private Long maxTimeoutMinutes;
        private Long periodMilliseconds;
    }

    @Data
    public static class OtpLifeTime {

        private Map<String, Long> sms;
        private Map<String, Long> email;
    }

    @Data
    public static class ResizeSignature {

        private Integer width = 600;
        private Integer heigth = 600;
        private Float quality = 0.7F;
        private Long maxSize = 1024L * 1024L * 10;
    }

    @Data
    public static class TTLConfig {

        private String channelId = "INT";
        private String operatorId = "ADMIN";
        private String operatorPassword = "abcd1234";
        private String equityBaseUrl = "http://172.25.11.16:7666";
        private String derivativeBaseUrl = "http://172.25.11.16:7666";
        private String operatorLoginUrl = "#{eqtUrl}/operatorLogin";
        private String openAccountUrl = "#{eqtUrl}/account/eqt/createClientTradingAM";
        private String listBankBranchUrl = "#{eqtUrl}/services/eqt/listbankbranch";
    }

    @Data
    public static class LotteConfig {

        private String apiKey;
        private String rootUrl;
        private String createAccountUrl;
        private String updateAccountUrl;
        private String updateStatusContractUrl;
        private String uploadImageUrl;
        private String responseCodeSuccess;
        private String responseErrorCodeBusiness;

        public List<String> getResponseCodeSuccess() {
            return Arrays.asList(this.responseCodeSuccess.split(";"));
        }

        public List<String> getResponseErrorCodeBusiness() {
            return Arrays.asList(this.responseErrorCodeBusiness.split(";"));
        }
    }

    @Data
    public static class Redis {

        private String host;
        private Integer port;
    }

    @Data
    public static class Topics {

        private String notification;
    }

    @Data
    public static class VNPT {

        public String publicKey;
        public String algorithmSignature;
    }

    @Data
    public static class Difisoft {

        public Boolean testing;
        public String publicKey;
        public String algorithmSignature;
    }

    public String getKafkaBootstraps() {
        return this.kafkaUrl.replace(";", ",");
    }

    public Pattern getAddressRegexPattern() {
        if (this.addressRegexPattern == null) {
            if (this.addressRegex != null && !this.addressRegex.isEmpty()) {
                this.addressRegexPattern = Pattern.compile(this.addressRegex);
            }
        }
        return addressRegexPattern;
    }

    @Data
    public static class FileStorage {
        private String storageType;
        private Minio minio;
        private String ekycImagesBucket;
        private Integer presignedURLDurationInSeconds;
        private String frontIDCardImageSuffix;
        private String backIDCardImageSuffix;

        @Data
        public static class Minio {
            private String baseUrl;
            private String accessKey;
            private String privateKey;
        }
    }
}
