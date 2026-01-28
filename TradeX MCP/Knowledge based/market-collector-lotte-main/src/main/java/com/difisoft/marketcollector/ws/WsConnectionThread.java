package com.difisoft.marketcollector.ws;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.model.v2.realtime.BidOfferUpdate;
import com.difisoft.market.model.v2.realtime.QuoteUpdate;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.realtime.MarketStatusData;
import com.difisoft.marketcollector.services.CacheService;
import com.difisoft.marketcollector.services.RequestSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j;

import java.util.Set;

@Log4j
public class WsConnectionThread implements Runnable {
    private WsConnection wsConnection;
    private final AppConf appConf;
    private final AppConf.WebSocketConf wsConf;
    private final RequestSender requestSender;
    private final ObjectMapper objectMapper;
    private final MarketRedisDao marketRedisDao;
    private final CacheService cacheService;
    private final Set<String> codes;

    public WsConnectionThread(
            AppConf appConf,
            AppConf.WebSocketConf wsConf,
            RequestSender requestSender,
            ObjectMapper objectMapper,
            MarketRedisDao marketRedisDao,
            CacheService cacheService,
            Set<String> codes
    ) {
        this.appConf = appConf;
        this.wsConf = wsConf;
        this.requestSender = requestSender;
        this.objectMapper = objectMapper;
        this.marketRedisDao = marketRedisDao;
        this.cacheService = cacheService;
        this.codes = codes;
    }

    @Override
    public void run() {
        wsConnection = new WsConnection(
                appConf.getStatusMap(),
                wsConf,
                appConf.getApiConnection(),
                objectMapper,
                (item) -> {
                    if (item instanceof QuoteUpdate) {
                        requestSender.sendMessageSafe("quoteUpdate", "Update", item);
                    } else if (item instanceof BidOfferUpdate) {
                        requestSender.sendMessageSafe("bidOfferUpdate", "Update", item);
                    } else if (item instanceof MarketStatusData) {
                        requestSender.sendMessageSafe("marketStatus", "Update", item);
                    }
                },
                marketRedisDao,
                cacheService,
                codes
        );
        wsConnection.start();
    }
}
