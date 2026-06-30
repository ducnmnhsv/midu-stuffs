package com.difisoft.nhsv.admin.config;

import com.difisoft.file.Conf;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Data
public class ApplicationProperties {

    private String clusterId;
    private String nodeId;
    private String vietStockDailyUrl = "https://api.vietstock.vn/ta/history?symbol={code}&resolution={resolution}&from={from}&to={to}";
    private String staticSymbolInfoMas = "https://masboard.masvn.com/media/symbol_static_data.json";
    private String staticSymbolInfoKis = "https://trading.kisvn.vn/files/resources/symbol_static_data.json";
    private String dataDir = "/data";
    private String kafkaUrls = "172.33.30.21:9092";
    private Map<String, String> minuteChartUrls = new HashMap<>();
    private Map<String, Map<String, String>> domainCodeMapping = new HashMap<>();
    private Map<String, Map<String, String>> revertCodeMapping = new HashMap<>();
    private Topics topics;
    private String rattingAIUrl;
    private String financeReportUrl;
    private String appVersionFileName = "app-version.json";
    private Conf fileConf;
    private String appVersionBucket = "paave-mobile-resource-uat";
    private String marketCorrectorBucket = "paave-mobile-resource-uat";
    private String appVersionFileUrl = "https://paave-mobile-resource-uat.s3.ap-southeast-1.amazonaws.com/app-version.json";
    private String supportEmail;
    private boolean enableJob = false;

    private String apiUrl = "https://nhsv-dev.tradex.vn/rest/";

    public String getKafkaBootstraps() {
        return this.kafkaUrls.replace(";", ",");
    }

    @PostConstruct
    public void init() {
        domainCodeMapping.forEach((domain, mapping) -> {
            Map<String, String> revertMapping = new HashMap<>();
            mapping.forEach((key, value) -> revertMapping.put(value, key));
            revertCodeMapping.put(domain, revertMapping);
        });
    }

    @Data
    public static class Topics {
        private String paaveVinanceUser;
        private String paaveVirtualCore;
    }
}
