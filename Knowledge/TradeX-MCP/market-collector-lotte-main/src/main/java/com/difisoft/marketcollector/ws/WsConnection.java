package com.difisoft.marketcollector.ws;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.model.common.BidOfferItem;
import com.difisoft.market.model.constant.MarketTypeEnum;
import com.difisoft.market.model.constant.SessionType;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.market.model.v2.realtime.BidOfferUpdate;
import com.difisoft.market.model.v2.realtime.QuoteUpdate;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.realtime.MarketStatusData;
import com.difisoft.marketcollector.services.CacheService;
import com.difisoft.marketcollector.utils.LotteApiUtil;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@ClientEndpoint
public class WsConnection {
    private static final Function<BigDecimal, BigDecimal> round4 = b -> b.setScale(4, RoundingMode.HALF_EVEN);
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter lotteTimeFormatter = DateTimeFormatter.ofPattern("Hmmss");

    private final Map<String, String> statusMap;
    private final AppConf.WebSocketConf config;
    private final AppConf.ApiConnection apiConfig;
    private final ObjectMapper objectMapper;
    private final Consumer<Object> outputConsumer;
    private final CacheService cacheService;
    private final String name;
    private final Set<String> codes;
    private final MarketRedisDao marketRedisDao;

    private final Map<String, String> codeMapping = new HashMap<>();
    private Session session;
    private final List<String> channels = new ArrayList<>();

    public WsConnection(
            Map<String, String> statusMap,
            AppConf.WebSocketConf config,
            AppConf.ApiConnection apiConfig,
            ObjectMapper objectMapper,
            Consumer<Object> outputConsumer,
            MarketRedisDao marketRedisDao,
            CacheService cacheService,
            Set<String> codes
    ) {
        this.name = config.getName();
        this.codes = codes;
        this.statusMap = statusMap;
        this.config = config;
        this.apiConfig = apiConfig;
        this.marketRedisDao = marketRedisDao;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
        this.outputConsumer = outputConsumer;
    }

    public void start() {
        if ("INDEX".equals(config.getCodeType())) {
            Stream<SymbolInfo> symbolInfos;
            if (codes != null && !codes.isEmpty()) {
                symbolInfos = codes.stream().map(this.marketRedisDao::getSymbolInfo);
            } else {
                symbolInfos = marketRedisDao.getAllSymbolInfo().stream().filter(it -> it.getType() == SymbolTypeEnum.INDEX);
            }

            String symbols = symbolInfos.map(item -> {
                codeMapping.put(item.getSecCode(), item.getCode());
                return item.getSecCode();
            }).collect(Collectors.joining("|"));
            channels.add("sub/pro.pub.auto.idxqt./" + symbols);
        } else if ("MARKET-STATUS".equals(config.getCodeType())) {
            channels.add("sub/pro.pub.auto.tickerNews.*/");
        } else {
            config.getChannels().forEach(it -> {
                channels.add(it + String.join("|", codes));
            });
        }
        this.startWebsocket(0);
    }

    private void startWebsocket(long index) {
        if (this.session != null) {
            try {
                this.session.close();
            } catch (IOException e) {
                log.error("{} fail to close session", name, e);
            }
        }
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            session = container.connectToServer(this, URI.create(config.getUrl()));
        } catch (DeploymentException | IOException e) {
            long timeout = index * 2000 + 5000;
            timeout = Math.min(timeout, 120000);
            log.error("{} fail to connect websocket {}. Schedule to retry in {} ms", name, config.getUrl(), timeout, e);
            try {
                Thread.sleep(timeout);
                this.startWebsocket(index + 1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        log.info("{} opening websocket", name);
        session = userSession;
        channels.forEach(it -> {
            log.info("{} sending message {}", name, it);
            session.getAsyncRemote().sendText(it);
        });
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason      the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.info("{} closing websocket with reason {}", name, reason);
        session = null;
        startWebsocket(0);
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param data The text message
     */
    @OnMessage
    public void onMessage(String data) {
        log.info("{} receive {}", name, data);
        if (data.startsWith("{")) {
            // ignore ping with json format
            return;
        }
        String[] parts = data.split("\\|");
        String service = parts[0];
        if (service.startsWith("auto.idxqt")) {
            this.handleIndexQuote(parts);
        } else if (service.startsWith("auto.qt")) {
            this.handleStockQuote(parts);
        } else if (service.startsWith("auto.bo")) {
            this.handleStockBidAsk(parts);
        } else if (service.startsWith("auto.tickerNews")) {
            this.handleSessionEvent(parts);
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("{} connection error", name, t);
    }

    private <T> T get(String uri, Class<T> clazz, Object optionalBody, ObjectMapper objectMapper) {
        return LotteApiUtil.get(name, uri, clazz, optionalBody, objectMapper, apiConfig, log);
    }

    private void handleStockQuote(String[] parts) {
        QuoteUpdate quoteUpdate = new QuoteUpdate();
        ZonedDateTime time = LocalTime.parse(parts[2], lotteTimeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID).withZoneSameInstant(DefaultUtils.UTC_ID);
        quoteUpdate.setTime(time.format(timeFormatter));
        String code = parts[3];
        code = codeMapping.getOrDefault(code, code);
        SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(code);
        if (symbolInfo == null) {
            log.error("ignore because no symbol info {}", parts);
        }
        quoteUpdate.setCode(code);

        ZonedDateTime highTime = LocalTime.parse(parts[4], lotteTimeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID);
        quoteUpdate.setHighTime(highTime.format(timeFormatter));
        ZonedDateTime lowTime = LocalTime.parse(parts[5], lotteTimeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID);
        quoteUpdate.setLowTime(lowTime.format(timeFormatter));

        quoteUpdate.setOpen(getDouble(parts[6], null));
        quoteUpdate.setHigh(getDouble(parts[8], null));
        quoteUpdate.setLow(getDouble(parts[10], null));
        quoteUpdate.setLast(getDouble(parts[12], null));
        quoteUpdate.setChange(getDouble(parts[14], null));
        quoteUpdate.setRate(getDouble(parts[16], null));

        quoteUpdate.setTurnoverRate(getDouble(parts[17], null));
        quoteUpdate.setAveragePrice(getDouble(parts[18], null));
        quoteUpdate.setReferencePrice(getDouble(parts[20], null));
        quoteUpdate.setTradingValue(getDouble(parts[21], null));
        quoteUpdate.setTradingVolume(getLong(parts[22], null));
        quoteUpdate.setMatchingVolume(getLong(parts[23], null));
        String matchedBy = parts[24];
        quoteUpdate.setMatchedBy("83".equals(matchedBy) ? "ASK" : ("66".equals(matchedBy) ? "BID" : null));
        quoteUpdate.setForeignerBuyVolume(getLong(parts[25], null));
        quoteUpdate.setForeignerSellVolume(getLong(parts[26], null));
        quoteUpdate.setForeignerCurrentRoom(getLong(parts[28], null));
        quoteUpdate.setForeignerTotalRoom(getLong(parts[27], null));

        if (parts.length >= 35) {
            quoteUpdate.setActiveSellVolume(getLong(parts[33], null));
            quoteUpdate.setActiveBuyVolume(getLong(parts[34], null));
        }

        quoteUpdate.setType(symbolInfo.getType());
        outputConsumer.accept(quoteUpdate);
    }

    private void handleStockBidAsk(String[] parts) {
        BidOfferUpdate bidAsk = new BidOfferUpdate();
        ZonedDateTime time = LocalTime.parse(parts[2], lotteTimeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID).withZoneSameInstant(DefaultUtils.UTC_ID);
        bidAsk.setTime(time.format(timeFormatter));
        String code = parts[3];
        code = codeMapping.getOrDefault(code, code);
        SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(code);
        bidAsk.setCode(code);
        String controlCode = parts[4];
        if (MarketTypeEnum.HOSE.name().equals(symbolInfo.getMarketType())) {
            switch (controlCode) {
                case "P" -> bidAsk.setSession(SessionType.ATO.name());
                case "O", "R" -> bidAsk.setSession(SessionType.LO.name());
                case "I" -> bidAsk.setSession(SessionType.INTERMISSION.name());
                case "A" -> bidAsk.setSession(SessionType.ATC.name());
                case "C" -> bidAsk.setSession(SessionType.PLO.name());
                case "K", "G" -> bidAsk.setSession(SessionType.CLOSED.name());
            }
        } else {
            switch (controlCode) {
                case "P", "O" -> bidAsk.setSession(SessionType.LO.name());
                case "2" -> bidAsk.setSession(SessionType.INTERMISSION.name());
                case "A" -> bidAsk.setSession(SessionType.ATC.name());
                case "C" -> bidAsk.setSession(SessionType.PLO.name());
                case "13", "97" -> bidAsk.setSession(SessionType.CLOSED.name());
            }
        }
        bidAsk.setExpectedPrice(getDouble(parts[5], null));
        bidAsk.setBidOfferList(new ArrayList<>());
        for (int i = 13; i < 73; i += 6) {
            BidOfferItem item = new BidOfferItem();
            item.setBidPrice(getDouble(parts[i], null));
            item.setBidVolume(getLong(parts[i + 2], null));
            item.setOfferPrice(getDouble(parts[i + 3], null));
            item.setOfferVolume(getLong(parts[i + 5], null));
            bidAsk.getBidOfferList().add(item);
        }
        for (int i = bidAsk.getBidOfferList().size() - 1; i > 0; i--) {
            BidOfferItem item = bidAsk.getBidOfferList().get(i);
            if ((item.getBidPrice() != null && item.getBidPrice() > 0) || (item.getOfferPrice() != null && item.getOfferPrice() > 0)) {
                break;
            }
            bidAsk.getBidOfferList().remove(i);
        }
        bidAsk.setTotalBidVolume(getLong(parts[73], null));
        bidAsk.setTotalOfferVolume(getLong(parts[74], null));
        bidAsk.setTotalBidCount(getLong(parts[83], null));
        bidAsk.setTotalOfferCount(getLong(parts[85], null));
        outputConsumer.accept(bidAsk);
    }

    private void handleIndexQuote(String[] parts) {
        QuoteUpdate quoteUpdate = new QuoteUpdate();
        ZonedDateTime time = LocalTime.parse(parts[2], lotteTimeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID);
        quoteUpdate.setTime(DefaultUtils.formatTime(time));
        String code = parts[3];
        quoteUpdate.setCode(codeMapping.getOrDefault(code, code));
        quoteUpdate.setOpen(getDouble(parts[5], null));
        quoteUpdate.setHigh(getDouble(parts[7], null));
        quoteUpdate.setLow(getDouble(parts[9], null));
        quoteUpdate.setLast(getDouble(parts[11], null));
        quoteUpdate.setChange(getDouble(parts[13], null));
        quoteUpdate.setRate(getDouble(parts[14], null));
        quoteUpdate.setTradingVolume(getLong(parts[15], null));
        quoteUpdate.setTradingValue(getDouble(parts[16], null));
        quoteUpdate.setMatchingVolume(getLong(parts[17], null));
        quoteUpdate.setCeilingCount(getInteger(parts[18], null));
        quoteUpdate.setUpCount(getInteger(parts[19], null));
        quoteUpdate.setUnchangedCount(getInteger(parts[20], null));
        quoteUpdate.setDownCount(getInteger(parts[21], null));
        quoteUpdate.setFloorCount(getInteger(parts[22], null));
        quoteUpdate.setType(SymbolTypeEnum.INDEX);
        outputConsumer.accept(quoteUpdate);
    }

    private void handleSessionEvent(String[] parts) {
        MarketStatusData quoteUpdate = new MarketStatusData();
        ZonedDateTime time = LocalTime.parse(parts[2], lotteTimeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID);
        quoteUpdate.setTime(time.format(timeFormatter));
        String code = parts[3];
        quoteUpdate.setCode(codeMapping.getOrDefault(code, code));
        quoteUpdate.setTitle(parts[9]);
        quoteUpdate.parseStatus(this.statusMap);
        outputConsumer.accept(quoteUpdate);
    }

    private Double getDouble(String value, Double defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            BigDecimal v = BigDecimal.valueOf(Double.parseDouble(value));
            return round4.apply(v).doubleValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Long getLong(String value, Long defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Integer getInteger(String value, Integer defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
