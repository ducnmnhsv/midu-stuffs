package com.techx.tradex.realtime.services;

import com.difisoft.market.common.redis.ListQuoteMeta;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.*;
import com.difisoft.market.model.constant.MarketTypeEnum;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.*;
import com.difisoft.model.utils.DefaultUtils;
import com.techx.tradex.realtime.utils.DatetimeUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Data
public class CacheService {
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private SymbolInfoRepository symbolInfoRepository;
    private SymbolInfoOddLotRepository symbolInfoOddLotRepository;
    private SymbolDailyRepository symbolDailyRepository;
    private ForeignerDailyRepository foreignerDailyRepository;
    private MarketRedisDao marketRedisDao;
    private IndexStockListRepository indexStockListRepository;
    private ThemeRepository themeRepository;
    private ThemeIndexRepository themeIndexRepository;
    private TradingDateService tradingDateService;
    private final ConcurrentHashMap<String, ListQuoteMeta> quoteMetaMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SymbolQuoteMinute> cacheCurrentQuoteMinute = new ConcurrentHashMap<>();

    @Autowired
    public CacheService(SymbolInfoRepository symbolInfoRepository, SymbolDailyRepository symbolDailyRepository,
                        ForeignerDailyRepository foreignerDailyRepository, MarketRedisDao marketRedisDao,
                        IndexStockListRepository indexStockListRepository, ThemeRepository themeRepository,
                        ThemeIndexRepository themeIndexRepository, TradingDateService tradingDateService) {
        this.symbolInfoRepository = symbolInfoRepository;
        this.symbolDailyRepository = symbolDailyRepository;
        this.foreignerDailyRepository = foreignerDailyRepository;
        this.marketRedisDao = marketRedisDao;
        this.indexStockListRepository = indexStockListRepository;
        this.themeRepository = themeRepository;
        this.themeIndexRepository = themeIndexRepository;
        this.tradingDateService = tradingDateService;
    }

    private Map<String, SymbolInfo> mapSymbolInfo;
    private Map<String, SymbolInfo> mapSymbolInfoOddLot;
    private Map<String, SymbolDaily> mapSymbolDaily;
    private Map<String, ForeignerDaily> mapForeignerDaily;
    private Map<String, SymbolStatistic> cacheStatistics;
    private List<Theme> themeList;
    private Map<String, SymbolDaily> mapPreviousSymbolDaily;
    private Map<String, ThemeIndex> mapPreviousThemeIndex;
    private Set<String> mapMinuteSymbolInfo = new HashSet<>();
    private Set<String> setVn30 = new HashSet<>(); // danh sach cac ma vn30
    private Set<String> setHnx30 = new HashSet<>(); // danh sach cac ma hnx30
    private Set<String> setVn30Trade = new HashSet<>(); // danh sach cac ma vn30 duoc trade
    private Set<String> setHnx30Trade = new HashSet<>(); // danh sach cac ma hnx30 duoc trade
    private Set<String> setVnTrade = new HashSet<>(); // danh sach cac ma vn duoc trade
    private Set<String> setHnxTrade = new HashSet<>(); // danh sach cac ma hnx duoc trade
    private Set<String> setUpcomTrade = new HashSet<>(); // danh sach cac ma upcom duoc trade
    private boolean enableAutoData = true;

    public void init() {
        this.reset();
    }

    public void reset() {
        log.info("start resetCache");
        long t1 = System.currentTimeMillis();
        List<SymbolInfo> symbolInfoList = marketRedisDao.getAllSymbolInfo();
        log.info("finish load symbolInfo from redis");
        List<SymbolInfoOddLot> symbolInfoOddLotList = marketRedisDao.getAllSymbolInfoOddLot();
        log.info("finish load symbolInfoOddLot from redis");
        List<SymbolDaily> symbolDailyList = marketRedisDao.getAllSymbolDaily();
        log.info("finish load symbolDaily from redis");
        List<ForeignerDaily> foreignerDailyList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        for (SymbolInfo symbolInfo : symbolInfoList) {
            try {
                ForeignerDaily foreignerDaily = new ForeignerDaily();
                foreignerDaily.setCode(symbolInfo.getCode());
                foreignerDaily.setSymbolType(symbolInfo.getType().name());
                try {
                    foreignerDaily.setDate(symbolInfo.getDate() == null ? new Date()
                            : DefaultUtils.DATE_FORMAT().parse(simpleDateFormat.format(symbolInfo.getDate())));
                } catch (ParseException e) {
                    log.error("fail to parse date", e);
                    foreignerDaily.setDate(new Date());
                }
                foreignerDaily.setForeignerCurrentRoom(symbolInfo.getForeignerCurrentRoom());
                foreignerDaily.setForeignerTotalRoom(symbolInfo.getForeignerTotalRoom());
                foreignerDaily.setForeignerBuyValue(symbolInfo.getForeignerBuyValue());
                foreignerDaily.setForeignerSellValue(symbolInfo.getForeignerSellValue());
                foreignerDaily.setForeignerBuyVolume(symbolInfo.getForeignerBuyVolume());
                foreignerDaily.setForeignerSellVolume(symbolInfo.getForeignerSellVolume());
                if (symbolInfo.getListedQuantity() != null) {
                    foreignerDaily.setListedQuantity(Double.parseDouble(symbolInfo.getListedQuantity().toString()));
                }
                foreignerDaily.setCreatedAt(new Date());
                foreignerDaily.setUpdatedAt(new Date());
                foreignerDailyList.add(foreignerDaily);
            } catch (Exception e) {
                log.error("fail to load symbol {}", symbolInfo, e);
            }
        }
        log.info("finish load foreignerDaily from redis");
        themeList = themeRepository.findAll();
        log.info("finish load theme from mongo");

        ZonedDateTime now = ZonedDateTime.now();
        List<SymbolDaily> listPreviousSymbolDaily = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            ZonedDateTime zonedDateTime1W = tradingDateService.minusDays(now, i);
            Date date1W = Date.from(zonedDateTime1W.toInstant());
            List<SymbolDaily> symbolDailyByDate = symbolDailyRepository.findByDate(date1W);
            if (!symbolDailyByDate.isEmpty()) {
                listPreviousSymbolDaily.addAll(symbolDailyByDate);
            }
        }
        log.info("finish load previous symbol daily from mongo");

        List<ThemeIndex> listPreviousThemeIndex = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            ZonedDateTime zonedDateTime1W = tradingDateService.minusDays(now, i);
            Date date1W = Date.from(zonedDateTime1W.toInstant());
            List<ThemeIndex> themeIndexList = themeIndexRepository.findByDate(date1W);
            if (!themeIndexList.isEmpty()) {
                listPreviousThemeIndex.addAll(themeIndexList);
            }
        }
        log.info("finish load previous theme index from mongo");

        this.mapSymbolInfo = new Hashtable<>();
        this.mapSymbolInfoOddLot = new Hashtable<>();
        this.mapSymbolDaily = new Hashtable<>();
        this.mapForeignerDaily = new Hashtable<>();
        this.cacheStatistics = new Hashtable<>();
        this.mapPreviousSymbolDaily = new Hashtable<>();
        this.mapPreviousThemeIndex = new Hashtable<>();
        this.quoteMetaMap.clear();
        this.cacheCurrentQuoteMinute.clear();

        for (SymbolInfo symbolInfo : symbolInfoList) {
            mapSymbolInfo.put(symbolInfo.getCode(), symbolInfo);
        }
        log.info("finish reload SymbolInfo");

        for (SymbolInfoOddLot symbolInfo : symbolInfoOddLotList) {
            mapSymbolInfoOddLot.put(symbolInfo.getCode(), symbolInfo);
        }
        log.info("finish reload SymbolInfoOddLot");

        for (SymbolDaily symbolDaily : symbolDailyList) {
            mapSymbolDaily.put(symbolDaily.getCode(), symbolDaily);
        }
        log.info("finish reload SymbolDaily");

        for (ForeignerDaily foreignerDaily : foreignerDailyList) {
            mapForeignerDaily.put(foreignerDaily.getCode(), foreignerDaily);
        }
        log.info("finish reload ForeignerDaily");

        for (SymbolDaily symbolDaily : listPreviousSymbolDaily) {
            mapPreviousSymbolDaily.put(DatetimeUtil.generateKey(symbolDaily.getCode(), symbolDaily.getDate()),
                    symbolDaily);
        }
        log.info("finish reload Symbol Daily Index 1 week a ago");

        for (ThemeIndex themeIndex : listPreviousThemeIndex) {
            mapPreviousThemeIndex.put(DatetimeUtil.generateKey(themeIndex.getThemeCode(), themeIndex.getDate()),
                    themeIndex);
        }
        log.info("finish reload Theme Index 1 week a ago");

        themeList.forEach(theme -> mapMinuteSymbolInfo.add(theme.getStockCode()));
        log.info("finish init Symbol Info theme calculation");

        Optional<IndexStockList> hnx30IndexStockList = indexStockListRepository.findById("HNX30");
        hnx30IndexStockList.ifPresent(indexStockList -> setHnx30 = new HashSet<>(indexStockList.getStockList()));
        Optional<IndexStockList> vn30IndexStockList = indexStockListRepository.findById("VN30");
        vn30IndexStockList.ifPresent(indexStockList -> setHnx30 = new HashSet<>(indexStockList.getStockList()));
        log.info("finish load HNX30, VN30");

        for (SymbolInfo symbolInfo : symbolInfoList) {
            if (symbolInfo.getType() == null || symbolInfo.getMarketType() == null) {
                log.error("type or marketType is null, symbolInfo: {}", symbolInfo);
                continue;
            }
            if (!symbolInfo.getType().equals(SymbolTypeEnum.STOCK)) {
                continue;
            }
            if (symbolInfo.getMarketType().equalsIgnoreCase(MarketTypeEnum.HOSE.name())
                    && symbolInfo.getTradingVolume() > 0) {
                setVnTrade.add(symbolInfo.getCode());
                if (setVn30.contains(symbolInfo.getCode())) {
                    setVn30Trade.add(symbolInfo.getCode());
                }
            }
            if (symbolInfo.getMarketType().equalsIgnoreCase(MarketTypeEnum.HNX.name())
                    && symbolInfo.getTradingVolume() > 0) {
                setHnxTrade.add(symbolInfo.getCode());
                if (setHnx30.contains(symbolInfo.getCode())) {
                    setHnx30Trade.add(symbolInfo.getCode());
                }
            }
            if (symbolInfo.getMarketType().equalsIgnoreCase(MarketTypeEnum.UPCOM.name())
                    && symbolInfo.getTradingVolume() > 0) {
                setUpcomTrade.add(symbolInfo.getCode());
            }
        }
        log.info("finish load setTrade");

        long t2 = System.currentTimeMillis();
        log.info("finish reset cacheService take: {}", (t2 - t1));
    }

    public void pauseAutoData() {
        log.info("pauseAutoData");
        this.enableAutoData = false;
    }

    public void resumeAutoData() {
        log.info("resumeAutoData");
        this.enableAutoData = true;
    }
}
