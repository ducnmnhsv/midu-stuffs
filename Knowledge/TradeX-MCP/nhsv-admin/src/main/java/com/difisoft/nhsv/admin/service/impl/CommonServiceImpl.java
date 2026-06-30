package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.kafka.Message;
import com.difisoft.model.responses.Response;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLoss;
import com.difisoft.nhsv.admin.domain.request.MarketHistoryRequest;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderProfitLossCustomRepository;
import com.difisoft.nhsv.admin.service.CommonService;
import com.difisoft.nhsv.admin.service.CopyMarketLeaderDetailsCustomService;
import com.difisoft.nhsv.admin.service.RequestSenderService;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
import com.difisoft.nhsv.admin.utils.DateTimeUtil;
import com.difisoft.nhsv.admin.utils.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service("commonService")
@Slf4j
public class CommonServiceImpl implements CommonService {

    private final RequestSenderService requestSender;
    private final ObjectMapper objectMapper;
    private final CopyMarketLeaderDetailsCustomService copyMarketLeaderDetailsCustomService;
    private final CopyMarketLeaderProfitLossCustomRepository copyMarketLeaderProfitLossCustomRepository;
    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    public CommonServiceImpl(RequestSenderService requestSender, ObjectMapper objectMapper, CopyMarketLeaderDetailsCustomService copyMarketLeaderDetailsCustomService, CopyMarketLeaderProfitLossCustomRepository copyMarketLeaderProfitLossCustomRepository) {
        this.requestSender = requestSender;
        this.objectMapper = objectMapper;
        this.copyMarketLeaderDetailsCustomService = copyMarketLeaderDetailsCustomService;
        this.copyMarketLeaderProfitLossCustomRepository = copyMarketLeaderProfitLossCustomRepository;
    }

    @Override
    public <T> void createKafkaRequest(String topic, String uri, Object request, String prefixLog, TypeReference<Response<T>> clazz) {
        log.info(prefixLog + "[createTuxedoRequest] uri = {}, request: {}, response type: {}", uri, request, clazz.getType());
        try {
            requestSender.sendAsyncRequest(topic, uri, applicationName, request);
        } catch (Exception e) {
            log.error(prefixLog + "[createTuxedoRequest] uri = {}, request: {}. Error msg: {}", uri, request, Util.objectToStringJsonIgnoreError(e.getStackTrace()));
        }
    }

    @Override
    public Message createKafkaRequest(String topic, String uri, Object request, String prefixLog) {
        log.info(prefixLog + "[createTuxedoRequest] uri = {}, request: {}, response type: {}", uri, request);
        try {
            return requestSender.sendAsyncRequest(topic, uri, applicationName, request).join();
        } catch (Exception e) {
            log.error(prefixLog + "[createTuxedoRequest] uri = {}, request: {}. Error msg: {}", uri, request, e.getMessage());
        }
        return null;
    }

    @Override
    public Message createMarketHistoryCorrectorKafka(String topic, String uri, MarketHistoryRequest request, String prefixLog, Integer timeout) {
        log.info(prefixLog + "[createRequest] uri = {}, request: {}", uri, request);

        if (timeout == null) {
            timeout = 300000;
        }

        CompletableFuture<Message> future = requestSender.sendAsyncRequest(topic, uri, applicationName, request, timeout);

        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.error(prefixLog + "[createRequest] uri = {}, request: {}. Timeout after {} milliseconds", uri, request, timeout);
            throw new RuntimeException("Timeout while waiting for Kafka response");
        } catch (InterruptedException e) {
            log.error(prefixLog + "[createRequest] uri = {}, request: {}. Request was interrupted", uri, request);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka request was interrupted");
        } catch (ExecutionException e) {
            log.error(prefixLog + "[createRequest] uri = {}, request: {}. Execution error: {}", uri, request, e.getMessage());
            throw new RuntimeException("Execution error occurred while processing Kafka request");
        }
    }

    @Override
    public void validateMarketLeaderRequired(Long mlUserId) {
        if (Objects.isNull(mlUserId)) {
            throw new GeneralException(Constants.MARKET_LEADER_ID_IS_REQUIRED);
        }
    }

    @Override
    public void validateMarketLeaderRequired(List<Long> mlUserId) {
        if (CollectionUtils.isEmpty(mlUserId)) {
            throw new GeneralException(Constants.MARKET_LEADER_ID_IS_REQUIRED);
        }
    }

    @Override
    public long convertTotalSubStrToLong(String totalSubStr, String ctxId) {
        long totalSub = 0L;
        try {
            totalSub = Long.parseLong(totalSubStr);
        } catch (Exception e) {
            log.info("[findAllUser] ctxId: {}, TOTAL_SUB value = {} is invalid: {}", totalSub, ctxId, Util.objectToStringJsonIgnoreError(e.getStackTrace()));
        }
        return totalSub;
    }

    @Override
    public ZonedDateTime getBeMarketLeaderDateByMlId(Long marketLeaderId) {
        String beMarketLeaderDateStr = this.copyMarketLeaderDetailsCustomService.findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(
            List.of(marketLeaderId)
            , Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING
            , Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO
            , Constants.CopyMarketLeaderDetailConstants.KEY_BE_MARKET_LEADER_DATE
            , Sort.by("createdAt").descending()
        ).stream().findFirst().map(CopyMarketLeaderDetailsDTO::getValue).orElse(null);
        if (StringUtils.isBlank(beMarketLeaderDateStr)) {
            throw new GeneralException(
                Constants.BE_MARKET_LEADER_DATE_INFO_IS_EMPTY
                , String.valueOf(marketLeaderId)
            );
        }

        return DateTimeUtil.stringToZoneDateTime(beMarketLeaderDateStr, Constants.DATE_FORMAT_dd_MM_yyyy_HH_mm_ss, Constants.DateTimeType.DATE_TIME);
    }

    @Override
    public ZonedDateTime getBeMarketLeaderDateByMlId(List<Long> marketLeaderId) {
        String beMarketLeaderDateStr = this.copyMarketLeaderDetailsCustomService.findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(
            marketLeaderId
            , Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING
            , Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO
            , Constants.CopyMarketLeaderDetailConstants.KEY_BE_MARKET_LEADER_DATE
            , Sort.by("createdAt").descending()
        ).stream().findFirst().map(CopyMarketLeaderDetailsDTO::getValue).orElse(null);
        if (StringUtils.isBlank(beMarketLeaderDateStr)) {
            throw new GeneralException(
                Constants.BE_MARKET_LEADER_DATE_INFO_IS_EMPTY
                , String.valueOf(marketLeaderId)
            );
        }

        return DateTimeUtil.stringToZoneDateTime(beMarketLeaderDateStr, Constants.DATE_FORMAT_dd_MM_yyyy_HH_mm_ss, Constants.DateTimeType.DATE_TIME);
    }

    @Override
    public Map<Long, ZonedDateTime> getBeMarketLeaderDateByMlIds(List<Long> marketLeaderIds) {
        List<CopyMarketLeaderDetailsDTO> mlDetailDTOs = this.copyMarketLeaderDetailsCustomService.findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(
            marketLeaderIds
            , Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING
            , Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO
            , Constants.CopyMarketLeaderDetailConstants.KEY_BE_MARKET_LEADER_DATE
            , Sort.by("createdAt").descending()
        ).stream().filter(dto -> StringUtils.isNotBlank(dto.getValue())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(mlDetailDTOs)) {
            throw new GeneralException(
                Constants.BE_MARKET_LEADER_DATE_INFO_IS_EMPTY
                , marketLeaderIds.stream().map(String::valueOf).collect(Collectors.joining(",")
            ));
        }


        return mlDetailDTOs.stream().collect(HashMap::new, (m, dto) -> m.put(dto.getMlUserId().getId()
            , DateTimeUtil.stringToZoneDateTime(dto.getValue(), Constants.DATE_FORMAT_dd_MM_yyyy_HH_mm_ss, Constants.DateTimeType.DATE_TIME)), HashMap::putAll);
    }

    @Override
    public Double getProfitLossRatio(Long id, String period, String txid) {
        log.info("[getProfitLossRatio] txid: {}, id: {}, period: {}", txid, id, period);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("reportDate").descending());
        Double profitLossRatio = 0D;
        List<CopyMarketLeaderProfitLoss> profitLosses = this.copyMarketLeaderProfitLossCustomRepository.findByMlUserIdAndType(id, period, pageable);
        if (!profitLosses.isEmpty()) {
            profitLossRatio = profitLosses.get(0).getProfitLossRatio();
        }
        return profitLossRatio;
    }

    @Override
    public String objectToStringJson(Object obj) throws JsonProcessingException {
        String json = Strings.EMPTY;
        if (Objects.nonNull(obj)) {
            json = getObjectMapper().writeValueAsString(obj);
        }
        return json;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
