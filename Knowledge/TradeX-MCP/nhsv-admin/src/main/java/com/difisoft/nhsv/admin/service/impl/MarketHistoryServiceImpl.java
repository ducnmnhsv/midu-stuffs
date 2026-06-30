package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.model.kafka.Message;
import com.difisoft.nhsv.admin.domain.IMarketHistoryJobResultStockEvent;
import com.difisoft.nhsv.admin.domain.MarketHistoryJobResult;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.domain.request.MarketHistoryRequest;
import com.difisoft.nhsv.admin.domain.response.MarketHistoryJobResultResponse;
import com.difisoft.nhsv.admin.domain.response.primary.MarketHistoryJobResultPrimaryResponse;
import com.difisoft.nhsv.admin.repository.UserRepository;
import com.difisoft.nhsv.admin.repository.primary.MarketHistoryJobResultPrimaryRepository;
import com.difisoft.nhsv.admin.security.SecurityUtils;
import com.difisoft.nhsv.admin.service.CommonService;
import com.difisoft.nhsv.admin.service.MarketHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MarketHistoryServiceImpl implements MarketHistoryService {

    @Value("${app.kafka.internal.market-history.topic}")
    private String marketHistoryTopic;

    @Value("${app.kafka.internal.market-history.uri}")
    private String marketHistoryTriggerUri;

    private final CommonService commonService;

    private final MarketHistoryJobResultPrimaryRepository jobResultRepository;

    private final UserRepository userRepository;

    @Override
    public void uploadMarketHistory(MarketHistoryRequest request) {

        log.info("Processing uploadMarketHistory for request: {}", request);

        ZonedDateTime timeStart = ZonedDateTime.now();

        String name = SecurityUtils.getCurrentUserLogin().orElse(null);

        User user = userRepository.findOneByLogin(name).orElse(null);

        User userId = new User();

        userId.setId(user.getId());

        if (request != null && request.getSymbols() != null) {
            List<String> upperCaseSymbols = request.getSymbols().stream()
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.toList());
            request.setSymbols(upperCaseSymbols);
        }

        try {
            Message response = commonService.createMarketHistoryCorrectorKafka(
                marketHistoryTopic,
                marketHistoryTriggerUri,
                request,
                "MarketHistoryCorretor",
                300000);

            if (response == null || !isValidResponse(response)) {

                throw new RuntimeException("Failed to get response from Kafka");
            }
            log.info("Received response: {}", response);

            saveJobResult(true, timeStart, ZonedDateTime.now(), "", request.getSymbols(), userId);

        } catch (Exception e) {
            log.error("Error processing uploadMarketHistory for request: {}. Error msg: {}", request, e.getMessage());
            saveJobResult(false, timeStart, ZonedDateTime.now(), "Unsuccessful", request.getSymbols(), userId);
        }
    }

    @Override
    public MarketHistoryJobResultResponse getLatestJobResult() {

        String name = SecurityUtils.getCurrentUserLogin().orElse(null);

        User user = userRepository.findOneByLogin(name).orElse(null);

        Optional<MarketHistoryJobResult> latestJobResult = jobResultRepository.findLatestJobResult(user.getId());

        return latestJobResult.map(marketHistoryJobResult -> new MarketHistoryJobResultResponse(
            marketHistoryJobResult.getId(),
            marketHistoryJobResult.getIsSuccess(),
            marketHistoryJobResult.getTimeStart(),
            marketHistoryJobResult.getTimeEnd(),
            Objects.isNull(marketHistoryJobResult.getError()) ? "" : marketHistoryJobResult.getError(),
            marketHistoryJobResult.getSymbols(),
            user.getId()
        )).orElse(null);

    }

    @Override
    public Page<MarketHistoryJobResultPrimaryResponse> getJobResult(Pageable pageable) {
        String name = SecurityUtils.getCurrentUserLogin().orElse(null);

        User user = userRepository.findOneByLogin(name).orElse(null);

        Page<IMarketHistoryJobResultStockEvent> page = jobResultRepository.findLatestJobResult(user.getId(), pageable);
        return page.map(it -> {
            MarketHistoryJobResultPrimaryResponse jobResult = new MarketHistoryJobResultPrimaryResponse();
            jobResult.setId(it.getId());
            jobResult.setSymbols(it.getSymbols());
            jobResult.setIsSuccess(it.getIsSuccess());
            jobResult.setTimeStart(it.getTimeStart());
            jobResult.setTimeEnd(it.getTimeEnd());
            jobResult.setError(it.getError());
            jobResult.setUserId(user.getId());
            jobResult.setEventId(it.getEventId());
            jobResult.setEventName(it.getEventName());
            jobResult.setEventType(it.getEventType());
            return jobResult;
        });
    }

    private void saveJobResult(boolean isSuccess, ZonedDateTime timeStart, ZonedDateTime timeEnd, String error, List<String> symbols, User userId) {
        MarketHistoryJobResult jobResult = new MarketHistoryJobResult();
        jobResult.setIsSuccess(isSuccess);
        jobResult.setTimeStart(timeStart);
        jobResult.setTimeEnd(timeEnd);
        jobResult.setError(error);
        jobResult.setUser(userId);
        jobResult.setEventId(null);
        String symbolsStr = String.join(", ", symbols);
        jobResult.setSymbols(symbolsStr);
        jobResultRepository.save(jobResult);
    }

    private boolean isValidResponse(Message response) {
        if (response.getData() instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) response.getData();

            if (data.containsKey("status")) {
                Map<String, Object> status = (Map<String, Object>) data.get("status");
                return !"REQUEST_PARSING_ERROR".equals(status.get("code"));
            }
        }
        return true;
    }
}
