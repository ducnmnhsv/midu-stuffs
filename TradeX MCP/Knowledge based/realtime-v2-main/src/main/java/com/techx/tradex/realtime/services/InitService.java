package com.techx.tradex.realtime.services;

import com.difisoft.file.FileService;
import com.difisoft.file.FileUtils;
import com.difisoft.market.common.KafkaSender;
import com.difisoft.market.common.MarketInit;
import com.difisoft.market.common.repository.IndexStockListRepository;
import com.difisoft.market.common.repository.SymbolDailyRepository;
import com.difisoft.market.common.repository.SymbolInfoRepository;
import com.difisoft.market.common.repository.SymbolInfoRollerRepository;
import com.difisoft.market.model.v2.realtime.SymbolInfoUpdate;
import com.difisoft.redis.RedisDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class InitService extends TimerTask implements KafkaSender {

    private final MonitorService monitorService;
    private final SymbolInfoService symbolInfoService;
    private final CacheService cacheService;
    private final MarketInit marketInit;
    private final KafkaProducer kafkaProducer;
    private final ConcurrentHashMap<String, GroupCommand> groupMap = new ConcurrentHashMap<>();

    private volatile boolean isRunning = false;

    public InitService(
            AppConf appConf,
            RedisDao redisDao,
            MonitorService monitorService,
            SymbolInfoService symbolInfoService,
            SymbolInfoRepository symbolInfoRepository,
            CacheService cacheService,
            IndexStockListRepository indexStockListRepository,
            ObjectMapper objectMapper,
            KafkaProducer kafkaProducer,
            SymbolInfoRollerRepository symbolInfoRollerRepository,
            SymbolDailyRepository symbolDailyRepository
    ) {
        this.monitorService = monitorService;
        this.kafkaProducer = kafkaProducer;
        this.symbolInfoService = symbolInfoService;
        this.cacheService = cacheService;
        FileService fileService = FileUtils.getFileService(appConf.getMarketConf().getFileConfig());
        this.marketInit = new MarketInit(redisDao,
                symbolInfoRepository,
                indexStockListRepository,
                this,
                appConf.getMarketConf(),
                objectMapper,
                fileService,
                symbolInfoRollerRepository,
                symbolDailyRepository
        );
        new Timer().scheduleAtFixedRate(this, 10000, 10000);
    }

    public void handleSymbolInfoUpdate(SymbolInfoUpdate request) {
        if (request.getCommand() == null) return;
        log.info("receive symbolInfoUpdate {} {}", request.getCommand().getGroupId(), request.getCode());
        groupMap.computeIfAbsent(request.getCommand().getGroupId(), k -> {
            GroupCommand groupCommand = new GroupCommand();
            groupCommand.id = request.getCommand().getGroupId();
            groupCommand.totalReceived = 0;
            return groupCommand;
        });
        groupMap.computeIfPresent(request.getCommand().getGroupId(), (k, gr) -> {
            if (request.getCommand().getTotalSymbols() != null) {
                gr.totalMessages = request.getCommand().getTotalSymbols();
            }
            gr.symbolInfoUpdates.add(request);
            gr.totalReceived += 1;
            gr.lastReceived = System.currentTimeMillis();
            return gr;
        });
    }

    @Override
    public void run() {
        if (isRunning) {
            log.warn("ignore because it's running");
            return;
        }
        isRunning = true;
        Set<GroupCommand> finishedGroups = new HashSet<>();
        Set<String> ids = new HashSet<>();
        groupMap.forEach((k, gr) -> {
            long time = System.currentTimeMillis();
            if (gr.totalReceived >= gr.totalMessages || time - gr.lastReceived >= 60000) {
                finishedGroups.add(gr);
                ids.add(gr.getId());
            }
        });
        if (!finishedGroups.isEmpty()) {
            log.warn("{} prepare init. Pause all threads...", ids);
            monitorService.pauseAll((m) -> {
                log.warn("{} Finish pause all threads...", ids);
                try {
                    finishedGroups.forEach(gr -> groupMap.remove(gr.id));
                    finishedGroups.forEach(gr -> {
                        AtomicReference<SymbolInfoUpdate.CommandGroup> cleanCommand = new AtomicReference<>();
                        try {
                            AtomicBoolean first = new AtomicBoolean(true);
                            gr.symbolInfoUpdates.forEach(it -> {
                                if (first.get()) { // first message. check clean condition and do cleaning
                                    first.set(false);
                                    SymbolInfoUpdate.CommandGroup clean = it.getCommand();
                                    cleanCommand.set(clean);
                                    log.warn("{} checking clean symbols {} {}", gr.id, gr.symbolInfoUpdates.size(), clean);
                                    if (clean.getCleanAll() != null && clean.getCleanAll()) {
                                        cacheService.getMapSymbolInfo().clear();
                                    } else {
                                        if (clean.getCleanByCode() != null) {
                                            cacheService.getMapSymbolInfo().remove(clean.getCleanByCode());
                                        }
                                        if (clean.getCleanByCodes() != null) {
                                            clean.getCleanByCodes().forEach(cacheService.getMapSymbolInfo()::remove);
                                        }
                                        List<String> cleanList = new ArrayList<>();
                                        if (clean.getCleanByMarket() != null || clean.getCleanByType() != null) {
                                            cacheService.getMapSymbolInfo().forEach((k, v) -> {
                                                if (
                                                        (v.getMarketType() != null && v.getMarketType().equals(clean.getCleanByMarket()))
                                                                || (v.getType() != null && v.getType().name().equals(clean.getCleanByType()))
                                                ) {
                                                    cleanList.add(k);
                                                }
                                            });
                                        }
                                        cleanList.forEach(cacheService.getMapSymbolInfo()::remove);
                                    }
                                }
                                if (it.getCode() != null) {
                                    symbolInfoService.updateBySymbolInfoUpdate(it, false);
                                }
                            });
                        } catch (Exception e) {
                            log.error("{} fail to init market by group {}, clean condition {} total update {}", ids, gr.id, cleanCommand.get(), gr.totalMessages, e);
                        }
                    });
                    log.warn("{} update to symbol cache finish. resume all threads", ids);
                    monitorService.resumeAll(it -> log.warn("{} finish resume all threads", ids));
                    log.warn("{} do init from cache symbols {}", ids, cacheService.getMapSymbolInfo().size());
                    marketInit.init(cacheService.getMapSymbolInfo().values());
                    cacheService.reset();
                } catch (Exception e) {
                    log.error("fail to init market", e);
                } finally {
                    monitorService.resumeAllNoCb();
                    isRunning = false;
                }
            });
        } else {
            isRunning = false;
        }
    }

    @Override
    public void send(String topic, String uri, Object data) {
        this.kafkaProducer.sendMiniMessageSafeNoResponse(topic, uri, data);
    }

    @Getter
    private static class GroupCommand {
        private String id;
        private int totalMessages;
        private int totalReceived;
        private long lastReceived;

        private final SortedSet<SymbolInfoUpdate> symbolInfoUpdates = new TreeSet<>();
    }
}

