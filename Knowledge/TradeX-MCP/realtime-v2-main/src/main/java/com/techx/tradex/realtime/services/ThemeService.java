package com.techx.tradex.realtime.services;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.SymbolInfoRepository;
import com.difisoft.market.model.v2.db.*;
import com.difisoft.model.utils.DefaultUtils;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.utils.DatetimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ThemeService {

    private final MarketRedisDao marketRedisDao;
    private final CacheService cacheService;
    private final KafkaProducer kafkaProducer;
    private final SymbolInfoRepository symbolInfoRepository;
    private final AppConf appConf;
    private final TradingDateService tradingDateService;

    public ThemeService(
            MarketRedisDao marketRedisDao,
            CacheService cacheService,
            KafkaProducer kafkaProducer, SymbolInfoRepository symbolInfoRepository, AppConf appConf, TradingDateService tradingDateService) {
        this.marketRedisDao = marketRedisDao;
        this.cacheService = cacheService;
        this.kafkaProducer = kafkaProducer;
        this.symbolInfoRepository = symbolInfoRepository;
        this.appConf = appConf;
        this.tradingDateService = tradingDateService;
    }

    public void updateThemeStatistic() {
        if (!appConf.isEnableTheme()) return;
        long currentTimeInMillis = System.currentTimeMillis() % DefaultUtils.DAY_IN_MS;
        long startTradingHour = getTimeInMillis(appConf.getInitData().getStartInitTheme());
        long endTradingHour = getTimeInMillis(appConf.getInitData().getEndTradingHour());

        if (currentTimeInMillis >= startTradingHour && currentTimeInMillis < endTradingHour) {
            boolean initTime = currentTimeInMillis <= getTimeInMillis(appConf.getInitData().getEndInitTime());
            Set<String> symbolQuoteMap = new HashSet<>(cacheService.getMapMinuteSymbolInfo());
            cacheService.getMapMinuteSymbolInfo().clear();

            Map<String, Theme> themeMap = new ConcurrentHashMap<>();

            List<Theme> themeList = cacheService.getThemeList();
            if (themeList != null && !themeList.isEmpty()) {
                themeList.stream()
                        .filter(theme -> symbolQuoteMap.contains(theme.getStockCode()))
                        .forEach(theme -> themeMap.put(theme.getThemeCode(), theme));

                if (!themeMap.isEmpty()) {
                    themeMap.values().parallelStream()
                            .forEach(theme -> {
                                List<Theme> themesWithSameCode = getThemesWithSameCode(theme.getThemeCode());
                                calculateThemeStatistic(themesWithSameCode, initTime);
                            });
                }
            } else {
                log.warn("EMPTY THEME LIST IN MONGO");
            }
        }
    }

    private String getFormattedDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return now.format(formatter);
    }

    private List<Theme> getThemesWithSameCode(String themeCode) {
        return cacheService.getThemeList().stream()
                .filter(theme -> theme.getThemeCode().equals(themeCode))
                .collect(Collectors.toList());
    }

    private void calculateThemeStatistic(List<Theme> themeList, Boolean initTime) {
        double totalRate = 0.0;
        int noOfIncreases = 0;
        int noOfDecreases = 0;
        int noOfUnchanges = 0;

        for (Theme theme : themeList) {
            SymbolInfo symbolInfo = getSymbolInfoFromTheme(theme);

            if (symbolInfo == null || symbolInfo.getRate() == null) {
                symbolInfo = new SymbolInfo();
                symbolInfo.setRate(getRateFromSymbolInfoRepository(theme.getStockCode()));
            }

            totalRate += symbolInfo.getRate();

            if (symbolInfo.getRate() > 0) {
                noOfIncreases++;
            } else if (symbolInfo.getRate() < 0) {
                noOfDecreases++;
            } else {
                noOfUnchanges++;
            }
        }

        double themeIndexChange = totalRate / themeList.size();
        ThemeStatistic.ThemeData themeData = new ThemeStatistic.ThemeData("1D", round(2, themeIndexChange), noOfIncreases, noOfDecreases, noOfUnchanges, null);
        List<ThemeStatistic.ThemeData> themeDataList = new ArrayList<>();
        themeDataList.add(themeData);

        Theme theme = themeList.get(0);
        ThemeStatistic redisThemeStatistic = marketRedisDao.getThemeStatistic(theme.getThemeCode());
        boolean shouldUpdate = false;

        if (initTime) {
            shouldUpdate = true;
        } else if (redisThemeStatistic != null) {
            ThemeStatistic.ThemeData oldData = redisThemeStatistic.getThemeData().get(0);
            ThemeStatistic.ThemeData newData = themeDataList.get(0);

            if (hasThemeDataChanged(oldData, newData)) {
                shouldUpdate = true;
            }
        } else {
            shouldUpdate = true;
        }

        if (shouldUpdate) {
            themeDataList.add(calculateThemeData(theme.getThemeCode(), themeList, "3D", themeData.getThemeChange(), 2));
            themeDataList.add(calculateThemeData(theme.getThemeCode(), themeList, "1W", themeData.getThemeChange(), 4));

            ThemeStatistic themeStatistic = new ThemeStatistic();
            themeStatistic.setThemeName(theme.getTheme());
            themeStatistic.setThemeCode(theme.getThemeCode());
            themeStatistic.setTime(getFormattedDateTime());
            themeStatistic.setThemeData(themeDataList);

            marketRedisDao.setThemeStatistic(theme.getThemeCode(), themeStatistic);
            kafkaProducer.sendMiniMessageSafeNoResponse("themeUpdate", null, themeStatistic);
        }

    }

    private double calculateThemeChange(double[] themeChanges) {
        double themeChange = 1.0;
        for (double change : themeChanges) {
            themeChange *= (1 + change / 100);
        }
        return (themeChange - 1) * 100;
    }

    private double[] getListThemeChange(String themeCode, int numDays, ZonedDateTime now, double themeChangeRealtime) {
        double[] themeChanges = new double[numDays + 1];
        for (int daysAgo = 1; daysAgo <= numDays; daysAgo++) {
            ZonedDateTime getDaysAgo = tradingDateService.minusDays(now, daysAgo);
            String key = DatetimeUtil.generateKey(themeCode, getDaysAgo);

            ThemeIndex themeIndex = cacheService.getMapPreviousThemeIndex().get(key);
            themeChanges[daysAgo - 1] = (themeIndex != null) ? themeIndex.getThemeChange() : 0.0;
        }
        themeChanges[numDays] = themeChangeRealtime;
        return themeChanges;
    }

    private double calculateChangeRate(int numDays, double[] rates) {
        double changeRate = 1.0;
        for (int i = 0; i <= numDays; i++) {
            changeRate *= (1 + rates[i] / 100);
        }
        return (changeRate - 1) * 100;
    }

    private ThemeStatistic.ThemeData calculateThemeData(String themeCode, List<Theme> themeList, String period, double themeChangeRealtime, int numDays) {
        int noOfIncreases = 0;
        int noOfDecreases = 0;
        int noOfUnchanges = 0;

        ZonedDateTime now = ZonedDateTime.now();
        double[] themeChanges = getListThemeChange(themeCode, numDays, now, themeChangeRealtime);
        double themeChange = calculateThemeChange(themeChanges);
        List<ThemeStatistic.ThemeData.StockData> stockDataList = new ArrayList<>();

        for (Theme theme : themeList) {
            double[] rates = new double[numDays + 1];
            for (int daysAgo = 1; daysAgo <= numDays; daysAgo++) {
                ZonedDateTime getDaysAgo = tradingDateService.minusDays(now, daysAgo);
                String key = DatetimeUtil.generateKey(theme.getStockCode(), getDaysAgo);

                SymbolDaily symbolDaily = cacheService.getMapPreviousSymbolDaily().get(key);
                rates[daysAgo - 1] = (symbolDaily != null) ? symbolDaily.getRate() : 0.0;
            }
            SymbolInfo symbolInfo = getSymbolInfoFromTheme(theme);
            if (symbolInfo.getRate() == null) {
                symbolInfo.setRate(getRateFromSymbolInfoRepository(theme.getStockCode()));
            }
            rates[numDays] = (symbolInfo.getRate());
            double changeRate = round(2, calculateChangeRate(numDays, rates));
            ThemeStatistic.ThemeData.StockData stockData = new ThemeStatistic.ThemeData.StockData();
            stockData.setStockCode(theme.getStockCode());
            stockData.setChangeRate(changeRate);
            stockDataList.add(stockData);

            if (changeRate > 0) {
                noOfIncreases++;
            } else if (changeRate < 0) {
                noOfDecreases++;
            } else {
                noOfUnchanges++;
            }
        }

        ThemeStatistic.ThemeData themeData = new ThemeStatistic.ThemeData();
        themeData.setPeriod(period);
        themeData.setDecreases(noOfDecreases);
        themeData.setIncreases(noOfIncreases);
        themeData.setUnchanges(noOfUnchanges);
        themeData.setThemeChange(round(2, themeChange));
        themeData.setStockData(stockDataList);
        return themeData;
    }

    private boolean hasThemeDataChanged(ThemeStatistic.ThemeData oldData, ThemeStatistic.ThemeData newData) {
        return oldData.getThemeChange() != newData.getThemeChange() ||
                !oldData.getDecreases().equals(newData.getDecreases()) ||
                !oldData.getIncreases().equals(newData.getIncreases()) ||
                !oldData.getUnchanges().equals(newData.getUnchanges());
    }

    private SymbolInfo getSymbolInfoFromTheme(Theme theme) {
        return marketRedisDao.getSymbolInfo(theme.getStockCode());
    }

    private Double getRateFromSymbolInfoRepository(String stockCode) {
        Optional<SymbolInfo> symbolInfoMongo = symbolInfoRepository.findById(stockCode);
        return symbolInfoMongo.map(info -> info.getRate()).orElse(0.0);
    }


    public Double round(Integer scale, Double value) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        bigDecimal = bigDecimal.setScale(scale, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    private static long getTimeInMillis(String time) {
        String[] parts = time.split(":");
        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        long seconds = Long.parseLong(parts[2]);
        return (((hours * 60 + minutes) * 60) + seconds) * 1000;
    }
}
