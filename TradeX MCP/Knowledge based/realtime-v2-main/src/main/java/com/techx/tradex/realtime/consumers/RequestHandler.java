package com.techx.tradex.realtime.consumers;


import com.difisoft.kafka.handler.Controller;
import com.difisoft.kafka.handler.DeserializeServerRequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RequestHandler extends DeserializeServerRequestHandler {

    @Autowired
    public RequestHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            RedisService redisService,
            CacheService cacheService,
            SymbolInfoRollerService symbolInfoRollerService,
            MonitorService monitorService,
            SymbolInfoService symbolInfoService
    ) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), 5);
        Map<String, Controller> map = new HashMap<>();
        map.put("/reloadSymbolInfo", new Controller<>(Object.class, (p1, p2) -> forwarded(redisService::reloadSymbolInfo)));
        map.put("/mergeWrongOrder", new Controller<>(String.class, monitorService::doMergeWrongOrderQuote));
        map.put("/recoverQuoteMinute", new Controller<>(String.class, monitorService::doRecoverQuoteMinute));
        map.put("/recoverAllQuoteMinute", new Controller<>(Integer.class, monitorService::recoverAllMinute));
        map.put("/reloadSymbolDaily", new Controller<>(Object.class, (p1, p2) -> forwarded(redisService::reloadSymbolDaily)));
        map.put("/resetCache", new Controller<>(Object.class, (p1, p2) -> forwarded(cacheService::reset)));
        map.put("/calculateRoller", new Controller<>(Object.class, (p1, p2) -> forwarded(symbolInfoRollerService::rollerData)));
        map.put("/uploadMarketStaticFile", new Controller<>(Object.class, (p1, p2) -> forwarded(symbolInfoService::uploadMarketStaticFile)));
        this.setControllerMap(map);
    }

    private boolean forwarded(Runnable func) {
        func.run();
        return true;
    }
}
