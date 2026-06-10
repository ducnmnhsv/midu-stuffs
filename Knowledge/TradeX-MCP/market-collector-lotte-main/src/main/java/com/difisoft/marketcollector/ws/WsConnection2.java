package com.difisoft.marketcollector.ws;

import com.difisoft.market.model.common.BidOfferItem;
import com.difisoft.market.model.v2.realtime.BidOfferUpdate;
import com.difisoft.market.model.v2.realtime.QuoteUpdate;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.lotte.api.IndexListResponse;
import com.difisoft.marketcollector.model.realtime.MarketStatusData;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class WsConnection2 implements WebSocketHandler {
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

    private final Map<String, String> statusMap;
    private final AppConf.WebSocketConf config;
    private final AppConf.ApiConnection apiConfig;
    private final ObjectMapper objectMapper;
    private final Consumer<Object> outputConsumer;
    private final String name;
    private final Set<String> codes;

    private final Map<String, String> codeMapping = new HashMap<>();
    private WebSocketSession session;
    private final List<String> channels = new ArrayList<>();

    public WsConnection2(
            Map<String, String> statusMap,
            AppConf.WebSocketConf config,
            AppConf.ApiConnection apiConfig,
            ObjectMapper objectMapper,
            Consumer<Object> outputConsumer,
            Set<String> codes
    ) {
        this.name = config.getName();
        this.codes = codes;
        this.statusMap = statusMap;
        this.config = config;
        this.apiConfig = apiConfig;
        this.objectMapper = objectMapper;
        this.outputConsumer = outputConsumer;
    }

    public void start() {
        if ("INDEX".equals(config.getCodeType())) {
            final List<IndexListResponse.Item> indexList = new ArrayList<>();;
            Map<String, String> body = new HashMap<>();
            body.put("mkt_tp", "%");
            IndexListResponse indexListResponse = this.get(apiConfig.getIndexListApi(), IndexListResponse.class, body, objectMapper);
            if (indexListResponse.isSuccess()) {
                AtomicBoolean hasNext = new AtomicBoolean(false);
                AtomicReference<String> nextData = new AtomicReference<>(null);
                indexListResponse.getDataList().forEach(it -> {
                    indexList.addAll(it.getList());
                    hasNext.set(it.isHasNext());
                    nextData.set(it.getNextKey());
                });
                while (hasNext.get()) {
                    body.put("next_data", nextData.get());
                    indexListResponse = this.get(apiConfig.getIndexListApi(), IndexListResponse.class, body, objectMapper);
                    if (indexListResponse.isSuccess()) {
                        indexListResponse.getDataList().forEach(it -> {
                            indexList.addAll(it.getList());
                            hasNext.set(it.isHasNext());
                            nextData.set(it.getNextKey());
                        });
                    } else {
                        log.error("{} fail to query index list {} - {}", name, indexListResponse.getErrorCode(), indexListResponse.getErrorDesc());
                        break;
                    }
                }
            } else {
                log.error("{}fail to query index list {} - {}", name, indexListResponse.getErrorCode(), indexListResponse.getErrorDesc());
            }
            if (indexList == null || indexList.isEmpty()) {
                log.error("{} there is no items in index list. Not start ws connection", name);
                return;
            }
            indexList.forEach(item -> {
                codeMapping.put(item.getCode(), config.getCodeMapping().getOrDefault(item.getSymbol(), item.getSymbol()));
            });
            channels.add("sub/pro.pub.auto.idxqt./" + indexList.stream().map(IndexListResponse.Item::getCode).collect(Collectors.joining("|")));
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
        WebSocketClient client = new StandardWebSocketClient();
        try (WebSocketSession session = client.execute(this, config.getUrl()).join()) {
            this.session = session;
        } catch (IOException e) {
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



    public <T> T get(String uri, Class<T> clazz, Object optionalBody, ObjectMapper objectMapper) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            RequestBuilder builder = RequestBuilder.
                    get(apiConfig.getBaseUrl() + uri).
                    setHeader("Content-Type", "application/json").
                    setHeader("apiKey", apiConfig.getApiKey());
            AtomicReference<String> bodyString = new AtomicReference<>();
            if (optionalBody != null) {
                bodyString.set(objectMapper.writeValueAsString(optionalBody));
                builder.setEntity(new StringEntity(bodyString.get()));
            }

            HttpUriRequest request = builder.build();


            return client.execute(request, httpResponse -> {
                log.info("{} request {}-{} and response {}", name, uri, bodyString.get(), httpResponse);
                String response = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                log.info("{} query {} with data {}", name, uri, response);
                return objectMapper.readValue(response, clazz);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                channels.forEach(it -> {
                    try {
                        log.info("{} sending message {}", name, it);
                        session.sendMessage(new TextMessage(it));
                    } catch (IOException e) {
                        log.error("{} fail to send message {}", name, it, e);
                    }
                });
            }
        }, 500);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("{} receive msg from ws {}", name, message);
        if (message instanceof PingMessage) {
            session.sendMessage(new PongMessage());
            return;
        }
        if (message instanceof PongMessage) {
            return;
        }
        if (message instanceof BinaryMessage) {
            return;
        }
        if (!(message instanceof TextMessage)) {
            return;
        }
        String data = ((TextMessage) message).getPayload();
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

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("{} fail to connect websocket {} - retrying...", name, config.getUrl(), exception);
        this.startWebsocket(0);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.warn("{} connection closed {}", name, closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleStockQuote(String[] parts) {
        QuoteUpdate quoteUpdate = new QuoteUpdate();
        ZonedDateTime time = LocalTime.parse(parts[2], timeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID);
        quoteUpdate.setTime(time.format(timeFormatter));
        String code = parts[3];
        quoteUpdate.setCode(codeMapping.getOrDefault(code, code));

        ZonedDateTime highTime = LocalTime.parse(parts[4], timeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID);
        quoteUpdate.setHighTime(highTime.format(timeFormatter));
        ZonedDateTime lowTime = LocalTime.parse(parts[5], timeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID);
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

        quoteUpdate.setForeignerBuyVolume(getLong(parts[25], null));
        quoteUpdate.setForeignerSellVolume(getLong(parts[26], null));

        if (parts.length >= 35) {
            quoteUpdate.setTotalOfferVolume(getLong(parts[33], null));
            quoteUpdate.setTotalBidVolume(getLong(parts[34], null));
        }

        outputConsumer.accept(quoteUpdate);
    }

    private void handleStockBidAsk(String[] parts) {
        BidOfferUpdate quoteUpdate = new BidOfferUpdate();
        ZonedDateTime time = LocalTime.parse(parts[2], timeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID);
        quoteUpdate.setTime(time.format(timeFormatter));
        String code = parts[3];
        quoteUpdate.setCode(codeMapping.getOrDefault(code, code));
        quoteUpdate.setExpectedPrice(getDouble(parts[5], null));
        quoteUpdate.setBidOfferList(new ArrayList<>());
        for (int i = 13; i < parts.length; i += 6) {
            BidOfferItem item = new BidOfferItem();
            item.setBidPrice(getDouble(parts[i], null));
            item.setBidVolume(getLong(parts[i + 2], null));
            item.setOfferPrice(getDouble(parts[i + 3], null));
            item.setOfferVolume(getLong(parts[i + 5], null));
            if ((item.getBidPrice() != null && item.getBidPrice() > 0) || (item.getOfferPrice() != null && item.getOfferPrice() > 0)) {
                quoteUpdate.getBidOfferList().add(item);
            }
        }
        outputConsumer.accept(quoteUpdate);
    }

    private void handleIndexQuote(String[] parts) {
        QuoteUpdate quoteUpdate = new QuoteUpdate();
        ZonedDateTime time = LocalTime.parse(parts[2], timeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID);
        quoteUpdate.setTime(time.format(timeFormatter));
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
        outputConsumer.accept(quoteUpdate);
    }

    private void handleSessionEvent(String[] parts) {
        MarketStatusData quoteUpdate = new MarketStatusData();
        ZonedDateTime time = LocalTime.parse(parts[2], timeFormatter).atDate(LocalDate.now()).atZone(DefaultUtils.VIETNAM_ID);
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
            return Double.parseDouble(value);
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
