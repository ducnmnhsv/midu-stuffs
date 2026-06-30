package com.difisoft.nhsv.admin.service;

import com.difisoft.model.kafka.Message;
import com.difisoft.model.responses.Response;
import com.difisoft.nhsv.admin.domain.request.MarketHistoryRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface CommonService {

    <T> void createKafkaRequest(String topic, String uri, Object request, String prefixLog, TypeReference<Response<T>> clazz);

    Message createKafkaRequest(String topic, String uri, Object request, String prefixLog);

    Message createMarketHistoryCorrectorKafka(String topic, String uri, MarketHistoryRequest request, String prefixLog, Integer timeout);

    void validateMarketLeaderRequired(Long mlUserId);

    void validateMarketLeaderRequired(List<Long> mlUserId);

    long convertTotalSubStrToLong(String totalSubStr, String ctxId);

    ZonedDateTime getBeMarketLeaderDateByMlId(Long marketLeaderId);

    Double getProfitLossRatio(Long id, String period, String txid);

    ZonedDateTime getBeMarketLeaderDateByMlId(List<Long> marketLeaderId);

    Map<Long, ZonedDateTime> getBeMarketLeaderDateByMlIds(List<Long> marketLeaderIds);

    String objectToStringJson(Object obj) throws JsonProcessingException;

    ObjectMapper getObjectMapper();
}
