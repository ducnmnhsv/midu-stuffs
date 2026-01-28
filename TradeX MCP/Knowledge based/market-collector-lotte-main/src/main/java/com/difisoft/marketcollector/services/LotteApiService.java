package com.difisoft.marketcollector.services;

import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.utils.LotteApiUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LotteApiService {
    private final ObjectMapper objectMapper;
    private final AppConf appConf;

    public LotteApiService(ObjectMapper objectMapper, AppConf appConf) {
        this.objectMapper = objectMapper;
        this.appConf = appConf;
    }

    public <T> T get(
            String logId,
            String uri,
            Class<T> clazz,
            Object optionalBody
    ) {
        return LotteApiUtil.get(logId, uri, clazz, optionalBody, objectMapper, appConf.getApiConnection(), log);
    }


    public <T> T get(
            String logId,
            String uri,
            Class<T> clazz,
            Object optionalBody,
            Logger log
    ) {
        return LotteApiUtil.get(logId, uri, clazz, optionalBody, objectMapper, appConf.getApiConnection(), log);
    }
}
