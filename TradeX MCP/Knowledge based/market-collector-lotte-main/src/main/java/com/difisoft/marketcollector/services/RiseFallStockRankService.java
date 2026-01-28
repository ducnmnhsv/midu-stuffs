package com.difisoft.marketcollector.services;

import com.difisoft.htsconnection.socket.message.receive.RiseFallRankRcv;
import com.difisoft.htsconnection.socket.message.send.RiseFallRankSnd;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.redis.CoordinatorService;
import com.difisoft.redis.RedisDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RiseFallStockRankService {
    private static final String REDIS_KEY = "market_rise_false_stock_ranking_";
    private static final Integer[] DAYS = new Integer[] {5, 20, 250};

    private final AppConf appConf;
    private final CoordinatorService coordinatorService;
    private final DownloadAccountService downloadAccountService;
    private final RedisDao redisDao;

    public RiseFallStockRankService(AppConf appConf, CoordinatorService coordinatorService, DownloadAccountService downloadAccountService, RedisDao redisDao) {
        this.appConf = appConf;
        this.coordinatorService = coordinatorService;
        this.downloadAccountService = downloadAccountService;
        this.redisDao = redisDao;
    }

    public void getAndSaveStockRanking() {
        String coordinatorKey = appConf.getClusterId() + "_" + "saveStockRank";
        if (!appConf.isEnableMultipleInstance() || coordinatorService.acquire(coordinatorKey, appConf.getNodeId(), 30000) != null) {
            try {
                executeGetAndSaveStockRanking();
            } catch (Exception e) {
                log.error("fail to save stock ranking", e);
            } finally {
                coordinatorService.release(coordinatorKey);
            }
        }
    }

    public void executeGetAndSaveStockRanking() {
        ZonedDateTime toDate = ZonedDateTime.now();
        String toDateStr = DefaultUtils.formatDate(toDate);
        String toDateTimeStr = DefaultUtils.formatDateTime(toDate);
        for (Integer day : DAYS) {
            for (RiseFallRankSnd.MarketType marketType : RiseFallRankSnd.MarketType.values()) {
                for (RiseFallRankSnd.UpDownType upDownType : RiseFallRankSnd.UpDownType.values()) {
                    DownloadAccountService.ConnectionController connectionController = downloadAccountService.getConnection();
                    String key = REDIS_KEY + marketType.name() + "_" + upDownType.name() + "_" + day;
                    ZonedDateTime fromDate = toDate.minusDays(day);
                    DayOfWeek fromDOW = fromDate.getDayOfWeek();
                    if (fromDOW == DayOfWeek.SATURDAY) {
                        fromDate.minusDays(1L);
                    } else if (fromDOW == DayOfWeek.SUNDAY) {
                        fromDate.minusDays(2L);
                    }
                    String fromDateStr = DefaultUtils.formatDate(fromDate);
                    RiseFallRankSnd req = new RiseFallRankSnd();
                    req.getMarketType().setValue(marketType.ordinal());
                    req.getUpDownType().setValue(upDownType.ordinal());
                    req.getToDate().setValue(toDateStr);
                    req.getFromDate().setValue(fromDateStr);
                    req.getOffSet().setValue(0);
                    req.getFetchCount().setValue(60);
                    connectionController.getConnectionHandler().sendMessageFuture(req, RiseFallRankRcv.class, 10000L)
                            .thenApply(r -> {
                                List<RiseFallRankItems> stocks = r.getItems().stream().map(i-> {
                                    RiseFallRankItems item = new RiseFallRankItems();
                                    item.setSequence(i.getSequence().getValue());
                                    item.setStockCode(i.getStockCode().getValue().trim().toUpperCase(Locale.ROOT));
                                    item.setLast(i.getLast().getValue());
                                    item.setChange(i.getChange().getValue());
                                    item.setRate((double) i.getRate().getValue());
                                    item.setVolume(i.getVolume().getValue());
                                    item.setUpDownRate((double) i.getUpDownRate().getValue());
                                    item.setUpDownRange(i.getUpDownRange().getValue());
                                    item.setStartPrice(i.getStartPrice().getValue());
                                    item.setEndPrice(i.getEndPrice().getValue());
                                    return item;
                                }).collect(Collectors.toList());
                                try {
                                    redisDao.set(key, new RiseFallRank(stocks, toDateTimeStr));
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                                connectionController.release();
                                return null;
                            }).exceptionally(e -> {
                                log.error("fail to save RiseFallRank", e);
                                return null;
                            });
                }
            }
        }
    }

    @Data
    public static class RiseFallRank {
        private List<RiseFallRankItems> symbols;
        private String resultAt;

        public RiseFallRank(List<RiseFallRankItems> symbols, String resultAt) {
            this.symbols = symbols;
            this.resultAt = resultAt;
        }
    }

    @Data
    public static class RiseFallRankItems {
        private Integer sequence;
        private String stockCode;
        private Integer last;
        private Integer change;
        private Double rate;
        private Long volume;
        private Double upDownRate;
        private Integer upDownRange;
        private Integer startPrice;
        private Integer endPrice;
    }
}
