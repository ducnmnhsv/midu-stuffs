package com.difisoft.marketcollector.services.recover;

import com.difisoft.file.FileService;
import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.redis.ListQuoteMeta;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.marketcollector.configurations.AppConf;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class QuoteStatisticService {
    private final static String DIR = QuoteStatisticService.class.getSimpleName();
    private final static String STATUS_FILE = "status.json";
    private final MarketRedisDao marketRedisDao;
    private final FileService fileService;
    private final ObjectMapper objectMapper;
    private final AppConf appConf;

    public QuoteStatisticService(
            MarketRedisDao marketRedisDao,
            ObjectMapper objectMapper,
            AppConf appConf,
            FileService fileService
    ) {
        this.marketRedisDao = marketRedisDao;
        this.objectMapper = objectMapper;
        this.appConf = appConf;
        this.fileService = fileService;
    }

    public CompletableFuture<Object> getStatistic(Object request, RequestContext<Object> ctx) {
        Status status = new Status();

        String statusFile = appConf.getRecover().getDataFolder() + DIR + "/" + STATUS_FILE;
        try {
            log.info("create folder");
            new File(appConf.getRecover().getDataFolder() + DIR).mkdirs();
        } catch (Exception e) {
            log.error("fail to create dir", e);
        }

        log.info("start get quote statistic");

        marketRedisDao.getAllSymbolInfo().forEach(it -> {
            ListQuoteMeta meta = marketRedisDao.getQuoteMeta(it.getCode());
            AtomicLong size = new AtomicLong(0L);
            size.addAndGet(marketRedisDao.symbolQuoteSize(it.getCode()));
            if (meta != null) {
                for (int i = 1; i < meta.size(); i++) {
                    size.addAndGet(meta.get(i).getTotalItems());
                }
            }
            status.total.addAndGet(size.get());
            if (it.getMarketType() != null)
                status.groupByExchange.compute(it.getMarketType(), (k, t) -> increase(t, size.get()));
            if (it.getType() != null) status.groupByType.compute(it.getType().name(), (k, t) -> increase(t, size.get()));
            if (it.getType() != null && it.getMarketType() != null)
                status.groupByExchangeAndType.compute(String.format("%s-%s", it.getType().name(), it.getMarketType()), (k, t) -> increase(t, size.get()));
            status.groupByCode.compute(it.getCode(), (k, t) -> increase(t, size.get()));
        });
        log.info("total: {}", status.total);
        log.info("exchange: {}", status.groupByExchange);
        log.info("type: {}", status.groupByType);
        log.info("exchangeAndType: {}", status.groupByExchangeAndType);

        try {
            objectMapper.writeValue(new File(statusFile), status);
        } catch (IOException exx) {
            log.error("fail to write status file", exx);
        }

        log.info("finish get quote statistic");
        return CompletableFuture.completedFuture(status.total);
    }

    @Data
    private class Status {
        AtomicLong total = new AtomicLong();
        Map<String, Long> groupByExchange = new HashMap<>();
        Map<String, Long> groupByType = new HashMap<>();
        Map<String, Long> groupByExchangeAndType = new HashMap<>();
        Map<String, Long> groupByCode = new HashMap<>();
    }


    private Long increase(Long oldValue, Long size) {
        if (oldValue == null) return size;
        return oldValue + size;
    }
}
