package com.techx.tradex.realtime.configurations;


import com.difisoft.market.common.Conf;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app")
@Data
public class AppConf {
    private String clusterId;
    private String kafkaUrl;
    private String monitorFile = "/data/monitor.data";
    private int noOfThreadHandler = 10;
    private Topics topics;
    private String holidayUrl;
    private String holidayLocalFile;
    private Integer batchSize = 1000;
    private InitDataConfig initData = new InitDataConfig();
    private Map<String, Integer> indexHighlightMap = new HashMap<>();
    private Integer defaultConsumerThread = 1;
    private Map<String, Integer> maxNoThreadMap = new HashMap<>();
    private Conf marketConf;
    private PublishConf publishV2Statistic;
    private boolean enableSaveStatistic = true;
    private boolean enableTheme = true;
    private boolean enableSaveBidOffer = true;
    private boolean enableCheckOrderQuote = false;
    private boolean enableSaveQuote = false;
    private boolean enableSaveQuoteMinute = false;
    private boolean enableSaveBidAsk = false;
    private boolean enableSaveWrongOrderQuote = false;
    private boolean enableInitData = false;
    private boolean enableQuotePartition = false;
    private boolean enableRoller = false;

    private long quotePartitionIntervalSecond = 30;
    private int cycleToRecoverMinute = 15;
    private int quotePartitionMinimumSize = 100;

    private Notifications notifications;

    public Integer getMaxThread(String topic) {
        Integer maxThread = maxNoThreadMap.get(topic);
        return (maxThread == null) ? this.defaultConsumerThread : maxThread;
    }

    @Data
    public static class PublishConf {
        private String topic;
        private String channel;
    }

    @Data
    public static class Notifications {
        private Integer stockTopWordsNumberRank;
        private String stockTopWordsRedirectUrl;
        private CeilingOrFloorPrice ceilingOrFloorPrice;
    }

    @Data
    public static class CeilingOrFloorPrice {
        private Boolean isActive;
    }

    @Data
    public static class Topics {
        private String quoteUpdate;
        private String quoteRecover;
        private String quoteOddLotUpdate;
        private String extraUpdate;
        private String symbolInfoUpdate;
        private String quoteMonitor;
        private String bidOfferUpdate;
        private String bidOfferOddLotUpdate;
        private String dealNoticeUpdate;
        private String advertisedUpdate;
        private String realtimeJob;
        private String tblUpdate;
        private String marketStatus;
        private String marketInit;
        private String requestResponseListener;
        private String requestListener;
        private String userUtility;
        private String virtualCore;
        private String virtualNotification;
        private String indexStockListUpdate;
    }

    public String getKafkaBootstraps() {
        return this.kafkaUrl.replace(";", ",");
    }

    @Data
    public static class InitDataConfig {
        private int version = 1; // support 1, 2
        private String startDay = "17:00:00";
        private String startInit = "00:00:00";
        private String endInitTime = "01:44:55";
        private String endTradingHour = "08:05:00";
        private String startInitTheme = "01:04:00";
    }
}
