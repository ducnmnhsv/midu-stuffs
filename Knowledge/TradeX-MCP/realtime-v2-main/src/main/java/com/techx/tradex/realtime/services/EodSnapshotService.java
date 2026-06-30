package com.techx.tradex.realtime.services;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.ForeignerDailyRepository;
import com.difisoft.market.common.repository.SymbolDailyRepository;
import com.difisoft.market.common.repository.SymbolInfoRepository;
import com.difisoft.market.model.common.BidOfferItem;
import com.difisoft.market.model.v2.db.ForeignerDaily;
import com.difisoft.market.model.v2.db.SymbolDaily;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.model.event.EodSnapshotEvent;
import com.techx.tradex.realtime.model.request.EodBackfillRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EodSnapshotService {

    private static final ZoneId ICT = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String SCHEMA_VERSION = "1.0";
    private static final long PUBLISH_TIMEOUT_SECONDS = 15;

    @Value("${app.enableEodSnapshotPublish:false}")
    private boolean enableEodSnapshotPublish;

    @Value("${app.topics.eodSnapshot:market.eod.snapshot}")
    private String eodSnapshotTopic;

    @Value("${app.eodMaxBackfillDays:30}")
    private int eodMaxBackfillDays;

    private final MarketRedisDao redisDao;
    private final KafkaProducer kafkaProducer;
    private final SymbolDailyRepository symbolDailyRepository;
    private final ForeignerDailyRepository foreignerDailyRepository;
    private final SymbolInfoRepository symbolInfoRepository;
    private final ObjectMapper objectMapper;
    private ObjectMapper eodObjectMapper;

    @PostConstruct
    public void init() {
        this.eodObjectMapper = objectMapper.copy()
                .setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    public void publishEodSnapshot() {
        if (!enableEodSnapshotPublish) {
            log.info("EOD snapshot: disabled by feature flag, skipping");
            return;
        }

        long startTime = System.currentTimeMillis();
        String snapshotRunId = UUID.randomUUID().toString();
        ZonedDateTime nowIct = ZonedDateTime.now(ICT);
        String tradingDate = nowIct.format(DATE_FMT);
        String snapshotAt = nowIct.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        List<SymbolInfo> symbolInfoList = redisDao.getAllSymbolInfo();
        List<SymbolDaily> symbolDailyList = redisDao.getAllSymbolDaily();
        List<ForeignerDaily> foreignerDailyList = redisDao.getAllForeignerDaily();

        if (isEmpty(symbolDailyList)) {
            log.warn("EOD snapshot: SymbolDaily is empty in Redis, skipping publish");
            return;
        }

        Map<String, SymbolInfo> symbolInfoMap = buildSymbolInfoMap(symbolInfoList);
        Map<String, ForeignerDaily> foreignerMap = buildForeignerMap(foreignerDailyList);

        int successCount = 0;
        int failureCount = 0;

        for (SymbolDaily daily : symbolDailyList) {
            try {
                SymbolInfo info = symbolInfoMap.get(daily.getCode());
                if (Objects.isNull(info)) {
                    log.warn("EOD snapshot: no SymbolInfo for {}, metadata fields will be null", daily.getCode());
                }
                EodSnapshotEvent event = buildEvent(
                        daily,
                        info,
                        foreignerMap.get(daily.getCode()),
                        tradingDate,
                        snapshotAt,
                        snapshotRunId);
                publishEvent(daily.getCode(), event, snapshotRunId);
                successCount++;
            } catch (Exception e) {
                log.error("EOD snapshot: failed to publish for symbol {}", daily.getCode(), e);
                failureCount++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("EOD snapshot summary: total={}, success={}, failure={}, duration={}ms",
                symbolDailyList.size(), successCount, failureCount, duration);
    }

    public boolean triggerBackfillJob(EodBackfillRequest request, RequestContext<EodBackfillRequest> ctx) {
        if (Objects.isNull(request)) {
            log.warn("Backfill: request is null");
            return false;
        }

        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(request.getFromDate());
            to = LocalDate.parse(request.getToDate());
        } catch (Exception e) {
            log.warn("Backfill: invalid date format — from_date={}, to_date={}",
                    request.getFromDate(), request.getToDate());
            return false;
        }

        if (from.isAfter(to)) {
            log.warn("Backfill: from_date {} is after to_date {}", from, to);
            return false;
        }

        long rangeDays = ChronoUnit.DAYS.between(from, to) + 1;
        if (rangeDays > eodMaxBackfillDays) {
            log.warn("Backfill: date range {} days exceeds max {} days", rangeDays, eodMaxBackfillDays);
            return false;
        }

        final LocalDate fromFinal = from;
        final LocalDate toFinal = to;
        CompletableFuture.runAsync(() -> executeBackfill(fromFinal, toFinal))
                .exceptionally(e -> {
                    log.error("Backfill: async job failed from={} to={}", fromFinal, toFinal, e);
                    return null;
                });
        log.info("Backfill: submitted async job for {} to {}", from, to);
        return true;
    }

    private void executeBackfill(LocalDate from, LocalDate to) {
        log.info("Backfill: starting from={} to={}", from, to);
        List<SymbolInfo> symbolInfoList = symbolInfoRepository.findAll();
        Map<String, SymbolInfo> symbolInfoMap = buildSymbolInfoMap(symbolInfoList);

        LocalDate current = from;
        while (!current.isAfter(to)) {
            DayOfWeek dow = current.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                current = current.plusDays(1);
                continue;
            }

            Date startOfDay = Date.from(current.atStartOfDay(ICT).toInstant());
            Date endOfDay = Date.from(current.plusDays(1).atStartOfDay(ICT).minus(1, ChronoUnit.MILLIS).toInstant());
            List<SymbolDaily> symbolDailyList = symbolDailyRepository.findByDateRange(startOfDay, endOfDay);
            if (isEmpty(symbolDailyList)) {
                log.warn("Backfill: no SymbolDaily data for {}, skipping", current);
                current = current.plusDays(1);
                continue;
            }

            List<ForeignerDaily> foreignerDailyList = foreignerDailyRepository.findByDateRange(startOfDay, endOfDay);
            Map<String, ForeignerDaily> foreignerMap = buildForeignerMap(foreignerDailyList);

            String tradingDate = current.format(DATE_FMT);
            String snapshotRunId = UUID.randomUUID().toString();
            String snapshotAt = ZonedDateTime.now(ICT).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            int successCount = 0;
            int failureCount = 0;

            for (SymbolDaily daily : symbolDailyList) {
                try {
                    SymbolInfo info = symbolInfoMap.get(daily.getCode());
                    if (Objects.isNull(info)) {
                        log.warn("Backfill: no SymbolInfo for {}, metadata fields will be null", daily.getCode());
                    }
                    EodSnapshotEvent event = buildEvent(
                            daily,
                            info,
                            foreignerMap.get(daily.getCode()),
                            tradingDate,
                            snapshotAt,
                            snapshotRunId);
                    publishEvent(daily.getCode(), event, snapshotRunId);
                    successCount++;
                } catch (Exception e) {
                    log.error("Backfill: failed for symbol {} on {}", daily.getCode(), current, e);
                    failureCount++;
                }
            }
            log.info("Backfill: date={}, total={}, success={}, failure={}",
                    tradingDate, symbolDailyList.size(), successCount, failureCount);
            current = current.plusDays(1);
        }
        log.info("Backfill: completed from={} to={}", from, to);
    }

    private EodSnapshotEvent buildEvent(SymbolDaily daily, SymbolInfo info, ForeignerDaily foreigner,
                                        String tradingDate, String snapshotAt, String snapshotRunId) {
        boolean hasInfo = Objects.nonNull(info);
        boolean hasForeigner = Objects.nonNull(foreigner);

        EodSnapshotEvent.PriceInfo price = new EodSnapshotEvent.PriceInfo(
                daily.getOpen(),
                daily.getHigh(),
                daily.getLow(),
                daily.getLast(),
                hasInfo ? info.getCeilingPrice() : null,
                hasInfo ? info.getFloorPrice() : null,
                hasInfo ? info.getReferencePrice() : null,
                hasInfo ? info.getAveragePrice() : null
        );

        EodSnapshotEvent.VolumeInfo volume = new EodSnapshotEvent.VolumeInfo(
                daily.getTradingVolume(),
                daily.getTradingValue()
        );

        EodSnapshotEvent.ForeignInfo foreign = new EodSnapshotEvent.ForeignInfo(
                hasForeigner ? foreigner.getForeignerBuyVolume() : null,
                hasForeigner ? foreigner.getForeignerSellVolume() : null,
                hasForeigner ? foreigner.getForeignerHoldVolume() : null,
                hasForeigner ? foreigner.getForeignerHoldRatio() : null,
                hasForeigner ? foreigner.getForeignerBuyAbleRatio() : null,
                hasForeigner ? foreigner.getForeignerCurrentRoom() : null,
                hasForeigner ? foreigner.getForeignerTotalRoom() : null
        );

        EodSnapshotEvent.MetadataInfo metadata = new EodSnapshotEvent.MetadataInfo(
                hasInfo ? info.getListedQuantity() : null,
                hasInfo ? info.getExchange() : null,
                hasInfo ? info.getSecuritiesType() : null
        );

        EodSnapshotEvent.OrderInfo order = buildOrderInfo(hasInfo ? info : null);

        return new EodSnapshotEvent(
                SCHEMA_VERSION,
                daily.getCode(),
                tradingDate,
                snapshotAt,
                snapshotRunId,
                price,
                volume,
                foreign,
                metadata,
                order
        );
    }

    private void publishEvent(String symbol, EodSnapshotEvent event, String snapshotRunId) throws Exception {
        String payload = eodObjectMapper.writeValueAsString(event);
        Map<String, String> headers = Map.of(
                "schema_version", SCHEMA_VERSION,
                "snapshot_run_id", snapshotRunId
        );
        kafkaProducer.sendRawMessage(eodSnapshotTopic, symbol, payload, headers)
                .get(PUBLISH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private EodSnapshotEvent.OrderInfo buildOrderInfo(SymbolInfo info) {
        if (Objects.isNull(info)) {
            return new EodSnapshotEvent.OrderInfo(null, null, null, null);
        }
        List<BidOfferItem> book = info.getBidOfferList();
        boolean hasBook = Objects.nonNull(book) && !book.isEmpty();

        Long buyVol = info.getTotalBidVolume();
        if (Objects.isNull(buyVol) && hasBook) {
            buyVol = sumBookVolume(book, true);
        }
        Long sellVol = info.getTotalOfferVolume();
        if (Objects.isNull(sellVol) && hasBook) {
            sellVol = sumBookVolume(book, false);
        }
        Long bestBidVol = hasBook ? book.get(0).getBidVolume() : info.getBidVolume();
        Long bestSellVol = hasBook ? book.get(0).getOfferVolume() : info.getOfferVolume();

        return new EodSnapshotEvent.OrderInfo(buyVol, sellVol, bestBidVol, bestSellVol);
    }

    private Long sumBookVolume(List<BidOfferItem> book, boolean bidSide) {
        long sum = 0L;
        boolean any = false;
        for (BidOfferItem item : book) {
            Long v = bidSide ? item.getBidVolume() : item.getOfferVolume();
            if (Objects.nonNull(v)) {
                sum += v;
                any = true;
            }
        }
        return any ? sum : null;
    }

    private Map<String, SymbolInfo> buildSymbolInfoMap(List<SymbolInfo> list) {
        Map<String, SymbolInfo> map = new HashMap<>();
        if (Objects.nonNull(list)) {
            for (SymbolInfo si : list) {
                map.put(si.getCode(), si);
            }
        }
        return map;
    }

    private Map<String, ForeignerDaily> buildForeignerMap(List<ForeignerDaily> list) {
        Map<String, ForeignerDaily> map = new HashMap<>();
        if (Objects.nonNull(list)) {
            for (ForeignerDaily fd : list) {
                map.put(fd.getCode(), fd);
            }
        }
        return map;
    }

    private boolean isEmpty(List<?> list) {
        return Objects.isNull(list) || list.isEmpty();
    }
}
