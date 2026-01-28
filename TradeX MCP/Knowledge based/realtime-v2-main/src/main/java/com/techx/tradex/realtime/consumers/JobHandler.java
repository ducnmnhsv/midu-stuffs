package com.techx.tradex.realtime.consumers;

import com.difisoft.kafka.handler.Controller;
import com.difisoft.kafka.handler.DeserializeServerRequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.model.request.SaveRedisToDatabaseRequest;
import com.techx.tradex.realtime.services.RedisService;
import com.techx.tradex.realtime.services.SymbolInfoRollerService;
import com.techx.tradex.realtime.services.SymbolInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JobHandler extends DeserializeServerRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(JobHandler.class);

    @Autowired
    public JobHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            RedisService redisService,
            SymbolInfoRollerService symbolInfoRollerService,
            SymbolInfoService symbolInfoService
    ) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getTopics().getRealtimeJob(), 5);
        Map<String, Controller> map = new HashMap<>();
        map.put("/job/resetRedisCache", new Controller<>(Object.class, (p1, p2) -> forwarded(redisService::resetRedisCache)));
        map.put("/job/removeAutoData", new Controller<>(Object.class, (p1, p2) -> forwarded(redisService::removeAutoData)));
        map.put("/job/refreshSymbolInfo", new Controller<>(Object.class, (p1, p2) -> forwarded(redisService::refreshSymbolInfo)));
        map.put("/job/saveRedisToDatabase", new Controller<>(SaveRedisToDatabaseRequest.class, redisService::saveRedisToDatabaseJob));
        map.put("/job/updateHighLowYear", new Controller<>(Object.class, symbolInfoRollerService::updateHighLowYear));
        map.put("job:/api/v1/realTime/vnIndexTopWorstReturns/trigger", new Controller<>(Object.class, symbolInfoService::stockTopWorstReturnsInfoExecute));
        this.setControllerMap(map);
    }

    private boolean forwarded(Runnable func) {
        func.run();
        return true;
    }
}
