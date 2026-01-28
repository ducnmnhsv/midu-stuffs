package com.difisoft.marketcollector.configurations;

import com.difisoft.htsconnection.socket.DataConnectionInfo;
import com.difisoft.htsconnection.socket.LoginData;
import com.difisoft.market.common.Conf;
import com.difisoft.market.model.constant.MarketTypeEnum;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;

@ConfigurationProperties(prefix = "app")
@Data
public class AppConf {
    private String domain;
    private Conf marketConf;
    private String clusterId;
    private String serviceName;
    private String nodeId;
    private String kafkaUrl;
    private Map<String, SystemConf> sysConf;
    private Topics topics;
    private RealtimeConf realtime;
    private int initThresholdSize;
    private RealtimeAccountConf accountDownload;
    private int serverHourOffset;
    private boolean logRealtimePacket = false;
    private boolean isUsingApi = true;

    private boolean enableMultipleInstance = false;
    private boolean enableQuery = true;
    private boolean enableInitMarket;
    private boolean enableUsingRedisForRealtime = true;
    private boolean enableIgnoreQuote = true;
    private boolean enableBond = false;
    private boolean enableStoreConnectionInfo = true;

    private Map<String, String> statusMap = new HashMap<>();
    private Map<String, Integer> highlightMap = new HashMap<>();
    private String timeStartReceiveBidAsk;
    private String timeStopReceiveBidAsk;
    private String key;
    private ApiConnection apiConnection;
    private Recover recover = new Recover();
    private String holidayUrl;
    private String holidayLocalFile;
    @Data
    public static class Holiday {
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private Date date;
        private String description;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class SystemConf {
        private String host;
        private List<String> backupHosts;
        private Integer loginPort;
        private Integer dataPort;
        private Integer marketPort;
        private String secCode;
    }

    @Data
    public static class Topics {
        private String market;
        private String marketInit;
        private String fix;
        private String requestResponseListener;
        private String requestListener;
        private String userUtility;
        private String notification;
        private String realtime;
        private String refreshData;
        private String symbolInfoUpdate;
        private String quoteRecover;
    }

    @Data
    public static class User {
        protected String username;
        protected String password;
    }

    @Data
    public static class Account extends LoginData {
        protected int id;
        protected String system;

        public void update(Account clone) {
            super.update(clone);
            if (clone.system != null) this.system = clone.system;
        }
    }

    @Data
    public static class RealtimeConf {
        private List<RealtimeAccountConf> accounts;
        private List<WebSocketConf> websocketConnections;
        private Map<String, SendOut> topics;
        private int numberCodesToRegister;
        private RealtimeWorkingTimeConf workingTime;
        private int minHour;
        private int maxHour;
        protected boolean startLogin;
        protected boolean useDefaultPort;
        protected int connectionDelay;

        private long receivePacketTimeout = 90000;
        private long receivePacketTimeoutScheduler = 30000;

        private int maxRetry = 10;
        private String monitorDataFile = "/data/monitor.json";
    }

    @Data
    public static class WebSocketConf {
        private String name;
        private String url;
        private Integer noOfThread;
        private String codeType;
        private MarketTypeEnum exchange;
        private List<String> channels;
        private Map<String, String> codeMapping;
    }

    @Data
    public static class RealtimeWorkingTimeConf {
        private String from;
        private String to;
        private List<Integer> weekDays;
    }

    @Data
    public static class SendOut {
        private String topic;
        private String uri;
        private String transformTo;
    }

    @Data
    public static class RealtimeAccountConf extends Account {
        private String name;
        private String system;
        private SymbolTypeEnum codeType;
        private MarketTypeEnum exchange;
        private Map<String, String> topicMapping;
        private List<String> topics;
        protected Set<String> onlyCodes;
        protected Integer noOfThread;
        protected List<RealtimeAccountConf> multipleConnections;
        protected boolean multipleConnectionsSplitBySorted;
        protected String branchCode1;
        protected String branchCode2;
        protected String personalId;
        protected String agencyNumber;
        protected String departmentNumber;
        protected Integer resubscribeAfterMs;

        public RealtimeAccountConf clone(User user) {
            RealtimeAccountConf clone = new RealtimeAccountConf();
            clone.update(this);
            clone.setUsername(user.getUsername());
            clone.setPassword(user.getPassword());
            return clone;
        }

        public void update(RealtimeAccountConf clone) {
            super.update(clone);
            if (clone.name != null) this.name = clone.name;
            if (clone.system != null) this.system = clone.system;
            if (clone.codeType != null) this.codeType = clone.codeType;
            if (clone.topicMapping != null) this.topicMapping = clone.topicMapping;
            if (clone.topics != null) this.topics = clone.topics;
            if (clone.onlyCodes != null) this.onlyCodes = clone.onlyCodes;
            if (clone.noOfThread != null) this.noOfThread = clone.noOfThread;
            if (clone.multipleConnections != null) this.multipleConnections = clone.multipleConnections;
            if (clone.branchCode1 != null) this.branchCode1 = clone.branchCode1;
            if (clone.branchCode2 != null) this.branchCode2 = clone.branchCode2;
            if (clone.personalId != null) this.personalId = clone.personalId;
            if (clone.agencyNumber != null) this.agencyNumber = clone.agencyNumber;
            if (clone.departmentNumber != null) this.departmentNumber = clone.departmentNumber;
            if (clone.resubscribeAfterMs != null) this.resubscribeAfterMs = clone.resubscribeAfterMs;
        }
    }

    @Data
    public static class SubscribeFilter {
        private SubscribeFilterItem includes;
    }

    @Data
    public static class SubscribeFilterItem {
        private List<String> exchanges;
    }

    public String getKafkaBootstraps() {
        return this.kafkaUrl.replace(";", ",");
    }

    public LoginData get(Account accountDownload) {
        AppConf.SystemConf systemConf = sysConf.get(accountDownload.getSystem());
        LoginData loginData = new LoginData(accountDownload.getUsername(), accountDownload.getPassword(), systemConf.getHost(), systemConf.getBackupHosts(), systemConf.getLoginPort(), systemConf.dataPort, systemConf.marketPort, systemConf.getSecCode());
        loginData.setIp(accountDownload.getIp());
        loginData.setMediaType(accountDownload.getMediaType());
        return loginData;
    }


    public DataConnectionInfo get(RealtimeAccountConf account) {
        AppConf.SystemConf systemConf = sysConf.get(account.getSystem());
        DataConnectionInfo connectionInfo = new DataConnectionInfo();
        connectionInfo.setUsername(account.getUsername());
        connectionInfo.setPassword(account.getPassword());
        connectionInfo.setHost(systemConf.host);
        connectionInfo.setLoginPort(systemConf.loginPort);
        connectionInfo.setDataPort(systemConf.dataPort);
        connectionInfo.setMarketPort(systemConf.marketPort);
        connectionInfo.setSecCode(systemConf.secCode);
        connectionInfo.setMediaType(account.getMediaType());
        connectionInfo.setBranchCode1(account.branchCode1);
        connectionInfo.setBranchCode2(account.branchCode2);
        connectionInfo.setPersonalId(account.personalId);
        connectionInfo.setAgencyNumber(account.agencyNumber);
        connectionInfo.setDepartmentNumber(account.departmentNumber);
        connectionInfo.setNotSendOtp(account.isNotSendOtp());
        return connectionInfo;
    }

    @Data
    public static class ApiConnection {
        private String baseUrl;
        private String indexListApi;
        private String queryStockListByIndex;
        private String stockDaily;
        private int stockDailySize = 100;
        private String indexDaily;
        private int indexDailySize = 100;
        private String stockQuotes;
        private String indexQuotes;
        private String cwQuotes;
        private String etfQuotes;
        private String symbolNames;
        private String symbolPrices;
        private String bestBidAsks;
        private String apiKey;
    }
    @Data
    public static class Recover {
        private String dataFolder = "/data/";
    }
}
