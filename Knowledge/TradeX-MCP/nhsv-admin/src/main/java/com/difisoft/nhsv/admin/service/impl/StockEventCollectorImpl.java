package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.kafka.Message;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.nhsv.admin.config.ApplicationProperties;
import com.difisoft.nhsv.admin.domain.MarketHistoryJobResult;
import com.difisoft.nhsv.admin.domain.StockEvent;
import com.difisoft.nhsv.admin.domain.enumeration.VietStockEventType;
import com.difisoft.nhsv.admin.domain.request.MarketHistoryRequest;
import com.difisoft.nhsv.admin.domain.request.TriggerCrawlEventStock;
import com.difisoft.nhsv.admin.repository.primary.MarketHistoryJobResultPrimaryRepository;
import com.difisoft.nhsv.admin.repository.primary.StockEventPrimaryRepository;
import com.difisoft.nhsv.admin.service.RequestSenderService;
import com.difisoft.nhsv.admin.service.StockEventCollector;
import com.difisoft.nhsv.admin.service.vietstock.IVietStockEventStrategy;
import com.difisoft.nhsv.admin.service.vietstock.IVietStockEventStrategyFactory;
import com.difisoft.nhsv.admin.service.vietstock.context.VietStockEventDataContext;
import com.difisoft.redis.CoordinatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service("StockEventCollector")
@Primary
public class StockEventCollectorImpl implements StockEventCollector {
    private static final String JOB_VIET_STOCK_COLLECTOR_ACQUIRE_LOCK_KEY = "vietstock_collector_acquire_lock";
    private final StockEventPrimaryRepository stockEventRepository;
    private final IVietStockEventStrategyFactory vietStockEventStrategyFactory;
    private final CoordinatorService coordinatorService;
    private final ApplicationProperties applicationProperties;
    private final MarketHistoryJobResultPrimaryRepository jobResultRepository;
    private final RequestSenderService requestSender;

    @Value("${app.kafka.internal.market-history.topic}")
    private String marketHistoryTopic;

    @Value("${app.kafka.internal.market-history.uri}")
    private String marketHistoryTriggerUri;

    public StockEventCollectorImpl(
        StockEventPrimaryRepository stockEventRepository,
        IVietStockEventStrategyFactory vietStockEventStrategyFactory,
        CoordinatorService coordinatorService,
        ApplicationProperties applicationProperties,
        MarketHistoryJobResultPrimaryRepository jobResultRepository,
        RequestSenderService requestSender
    ) {
        this.stockEventRepository = stockEventRepository;
        this.vietStockEventStrategyFactory = vietStockEventStrategyFactory;
        this.coordinatorService = coordinatorService;
        this.applicationProperties = applicationProperties;
        this.jobResultRepository = jobResultRepository;
        this.requestSender = requestSender;
    }

    @Override
    @Scheduled(cron = "${app.cron.viet-stock-event-collector}")
    public void vietStockEventCollectorJob() {

        String nodeId = this.applicationProperties.getNodeId();
        log.info("vietStockEventCollectorJob started - NodeId: {}", nodeId);

        String lockValue = null;
        try {
            lockValue = this.coordinatorService.acquire(JOB_VIET_STOCK_COLLECTOR_ACQUIRE_LOCK_KEY, nodeId, 300);

            if (lockValue == null) {
                log.info("Job skipped - Another instance is already running (nodeId: {})", nodeId);
                return;
            }
            log.info("Job acquired lock successfully (nodeId: {})", nodeId);
            log.info("Starting to crawl stock events for all event types");
            this.crawlDataVietStock(ZonedDateTime.now());
            log.info("Adjusting historical price after crawling events...");
            this.adjustHistoricalPrice();
            log.info("Finished adjusting historical price.");
        } catch (Exception e) {
            log.error("Error during job execution (nodeId: {})", nodeId, e);
            throw e;
        } finally {
            if (lockValue != null) {
                log.info("Job crawl stock events release acquired lock (nodeId: {}, lockValue: {})", nodeId, lockValue);
                this.coordinatorService.release(JOB_VIET_STOCK_COLLECTOR_ACQUIRE_LOCK_KEY);
            } else {
                log.debug("No lock to release (nodeId: {})", nodeId);
            }
        }
    }

    @Override
    public Object triggerCrawlEventFromVietStock(TriggerCrawlEventStock request, RequestContext<TriggerCrawlEventStock> ctx) {
        log.info("Triggering crawl event from VietStock. FromDate: {}, ToDate: {}",
            request.getFromDate(), request.getToDate());
        ZonedDateTime fromDateZdt = ZonedDateTime.ofInstant(request.getFromDate().toInstant(), ZoneId.systemDefault());
        ZonedDateTime toDateZdt = ZonedDateTime.ofInstant(request.getToDate().toInstant(), ZoneId.systemDefault());

        this.crawlDataVietStock(fromDateZdt, toDateZdt);
        return new HashMap<>();
    }

    private void crawlDataVietStock(ZonedDateTime fromDate) {
        this.crawlDataVietStockInternal(fromDate, null);
    }

    private void crawlDataVietStock(ZonedDateTime fromDate, ZonedDateTime toDate) {
        this.crawlDataVietStockInternal(fromDate, toDate);
    }

    private void crawlDataVietStockInternal(ZonedDateTime fromDate, ZonedDateTime toDate) {
        List<StockEvent> allEvents = new ArrayList<>();

        for (VietStockEventType eventType : VietStockEventType.values()) {
            VietStockEventDataContext context = new VietStockEventDataContext();
            context.setEventType(eventType);
            context.setFromDate(fromDate);
            if (toDate != null) {
                context.setToDate(toDate);
            }

            log.info("Processing events for type: {}", eventType);
            IVietStockEventStrategy eventStrategy = this.vietStockEventStrategyFactory.getEventStrategy(eventType);
            eventStrategy.process(context);
            log.info("Completed processing {} events for type: {}", context.getEvents().size(), eventType);
            allEvents.addAll(context.getEvents());
        }

        log.info("Total events: {}", allEvents.size());
        if (!allEvents.isEmpty()) {
            this.stockEventRepository.saveAll(allEvents);
        }
    }

    private void adjustHistoricalPrice() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime nowVnt = now.withZoneSameInstant(DefaultUtils.VIETNAM_ID);

        List<StockEvent> stockEvents = stockEventRepository.findByIsAdjustedFalseAndEffectiveDateLessThanEqual(nowVnt);
        if (stockEvents.isEmpty()) {
            log.info("No adjusted stock events found with effectiveDate <= {}. Skipping historical price adjustment.", nowVnt);
            return;
        }

        Map<String, List<StockEvent>> groupedEvents = stockEvents.stream()
            .collect(Collectors.groupingBy(StockEvent::getCode));

        groupedEvents.forEach((code, events) -> {
            MarketHistoryRequest request = new MarketHistoryRequest(Collections.singletonList(code));
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            StringBuilder error = new StringBuilder();

            try {
                CompletableFuture<Message> future = requestSender.sendAsyncRequest(
                    marketHistoryTopic,
                    marketHistoryTriggerUri,
                    applicationProperties.getClusterId(),
                    request
                );
                Message response = future.get(5, TimeUnit.MINUTES);
                log.info("Received response for {}: {}", code, response);

                if (isValidResponse(response)) {
                    events.forEach(event -> event.setIsAdjusted(true));
                    stockEventRepository.saveAll(events);
                }
            } catch (Exception e) {
                log.error("Error processing adjust historical price for {}: ", code, e);
                isSuccess.set(false);
                if (e instanceof GeneralException) {
                    error.append(((GeneralException) e).getCode());
                } else {
                    error.append("Unsuccessful");
                }
            } finally {
                ZonedDateTime timeEnd = ZonedDateTime.now();
                List<MarketHistoryJobResult> jobResults = events.stream().map(event -> {
                    MarketHistoryJobResult result = new MarketHistoryJobResult();
                    result.setSymbols(code);
                    result.setIsSuccess(isSuccess.get());
                    result.setTimeStart(now);
                    result.setTimeEnd(timeEnd);
                    result.setError(isSuccess.get() ? null : error.toString());
                    result.setUser(null);
                    result.setEventId(event.getId());
                    return result;
                }).collect(Collectors.toList());

                jobResultRepository.saveAll(jobResults);
            }
        });
    }

    private boolean isValidResponse(Message response) {
        if (response.getData() instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) response.getData();

            if (data.containsKey("status")) {
                Map<String, Object> status = (Map<String, Object>) data.get("status");
                throw new GeneralException((String) status.get("code"));
            }
        }
        return true;
    }
}
