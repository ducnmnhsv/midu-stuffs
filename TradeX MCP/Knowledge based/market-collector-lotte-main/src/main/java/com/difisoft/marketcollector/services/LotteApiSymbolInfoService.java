package com.difisoft.marketcollector.services;

import com.difisoft.file.FileService;
import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.MarketInit;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.IndexStockListRepository;
import com.difisoft.market.common.repository.SymbolDailyRepository;
import com.difisoft.market.common.repository.SymbolInfoRepository;
import com.difisoft.market.common.repository.SymbolInfoRollerRepository;
import com.difisoft.market.model.common.BidOfferItem;
import com.difisoft.market.model.common.HighLowYearItem;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.constants.Constants;
import com.difisoft.marketcollector.model.lotte.api.*;
import com.difisoft.marketcollector.utils.CompletableUtil;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.kafka.Message;
import com.difisoft.model.utils.DefaultUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class LotteApiSymbolInfoService implements ISymbolInfoService {
    private static final MathContext round4 = new MathContext(4, RoundingMode.HALF_UP);
    private final MarketInit marketInit;
    private final AppConf appConf;
    private final RequestSender requestSender;
    private final LotteApiService lotteApiService;
    private final CoordinatorService coordinatorService;
    private final HolidayService holidayService;

    public LotteApiSymbolInfoService(SymbolInfoRepository symbolInfoRepo,
                                     AppConf appConf,
                                     RequestSender requestSender,
                                     MarketRedisDao marketRedisDao,
                                     IndexStockListRepository indexStockListRepository,
                                     ObjectMapper objectMapper,
                                     SymbolInfoRollerRepository symbolInfoRollerRepository,
                                     SymbolDailyRepository symbolDailyRepository,
                                     FileService fileService,
                                     CoordinatorService coordinatorService,
                                     HolidayService holidayService,
                                     LotteApiService lotteApiService
    ) {
        this.appConf = appConf;
        this.requestSender = requestSender;
        this.coordinatorService = coordinatorService;
        this.holidayService = holidayService;
        this.lotteApiService = lotteApiService;
        this.marketInit = new MarketInit(
                marketRedisDao.getRedisDao(),
                symbolInfoRepo,
                indexStockListRepository,
                this,
                appConf.getMarketConf(),
                objectMapper,
                fileService,
                symbolInfoRollerRepository,
                symbolDailyRepository
        );
    }

    @Override
    public void send(String topic, String uri, Object data) {
        Message<Object> msg = new Message<>();
        msg.setData(data);
        msg.setUri("Update");
        try {
            this.requestSender.sendMessageNoResponse(topic, uri, data);
        } catch (IOException e) {
            log.error("fail to send message {} {} {}", topic, uri, data, e);
        }
    }

    public Object downloadSymbolFromRequest(Object ignoredRequest, RequestContext<Object> ctx) {
        return this.downloadSymbol(ctx.getId());
    }

    public Object forceDownloadSymbolFromRequest(Object ignoredRequest, RequestContext<Object> ctx) {
        return this.downloadSymbol(ctx.getId());
    }

    public CompletableFuture<Void> downloadSymbol(String id) {
        String coordinatorKey = appConf.getServiceName() + "_" + "downloadSymbolJob";
        if (!appConf.isEnableMultipleInstance() || coordinatorService.acquire(coordinatorKey, appConf.getNodeId(), 60) != null) {
            try {
                this.downloadSymbol(id, 0);
            } finally {
                coordinatorService.release(coordinatorKey);
            }
        } else {
            log.info("{} this will not start download symbol info service do not ", id);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void downloadIndexList(String id, Map<String, IndexListResponse.Item> map, String nextKey) {
        IndexListResponse response = lotteApiService.get(id, appConf.getApiConnection().getIndexListApi(),
                IndexListResponse.class, new IndexListRequest(nextKey), log);
        AtomicReference<String> next = new AtomicReference<>(null);
        response.getDataList().forEach(it -> {
            if (it.isHasNext()) {
                next.set(it.getNextKey());
            } else {
                next.set(null);
            }
            it.getList().forEach(item -> map.put(item.getCode(), item));
        });
        if (next.get() != null) {
            downloadIndexList(id, map, next.get());
        }
    }

    private void downloadIndexInfo(String id, Map<String, SymbolInfo> map, Map<String, IndexListResponse.Item> mapIndexes) {
        mapIndexes.forEach((code, item) -> {
            IndexDailyResponse response = lotteApiService.get(id, appConf.getApiConnection().getIndexDaily(),
                    IndexDailyResponse.class, IndexDailyRequest.builder().rowCount(2).indexRefCode(item.getCode()).build(), log);
            IndexDailyResponse.Item todayPrice = null;
            IndexDailyResponse.Item yesterdayPrice = null;
            if (!response.getDataList().isEmpty()) {
                List<IndexDailyResponse.Item> l = response.getDataList().get(0).getList();
                if (!l.isEmpty()) {
                    todayPrice = l.get(l.size() - 1);
                    if (l.size() > 1) {
                        yesterdayPrice = l.get(l.size() - 2);
                    }
                }
            }
            if (todayPrice == null) {
                log.error("{} there is no daily record for index {}", id, item.getSymbol());
                return;
            }
            SymbolInfo symbolInfo = new SymbolInfo();
            if (item.getSymbol().endsWith("INDEX")) {
                symbolInfo.setCode(item.getSymbol().substring(0, item.getSymbol().length() - 5));
            } else {
                symbolInfo.setCode(item.getSymbol());
            }
            if (yesterdayPrice != null) {
                symbolInfo.setReferencePrice(yesterdayPrice.getClose());
            } else {
                log.error("{} there is no previous daily close price record for index {}", id, item.getSymbol());
            }
            symbolInfo.setSecCode(item.getCode());
            symbolInfo.setRefCode(item.getCode());
            symbolInfo.setTime(DefaultUtils.formatTime(ZonedDateTime.now()));
            symbolInfo.setDate(DefaultUtils.formatDate(ZonedDateTime.now()));
            symbolInfo.setName(item.getVietnameseName());
            symbolInfo.setNameEn(item.getEnglishName());
            symbolInfo.setLast(todayPrice.getClose());
            symbolInfo.setChange(todayPrice.getChange());
            symbolInfo.setRate(todayPrice.getChangeRate());
            symbolInfo.setTradingVolume(todayPrice.getVolume());
            symbolInfo.setTradingValue(todayPrice.getValue() * 1000000);
            symbolInfo.setOpen(todayPrice.getOpen());
            symbolInfo.setHigh(todayPrice.getHigh());
            symbolInfo.setLow(todayPrice.getLow());
            symbolInfo.setType(SymbolTypeEnum.INDEX);
            symbolInfo.setExchange(item.getExchange().toMarketType().name());
            symbolInfo.setMarketType(symbolInfo.getExchange());
            symbolInfo.setMarketName(symbolInfo.getExchange());
            symbolInfo.setSecurityExchange(symbolInfo.getExchange());
            symbolInfo.setSecuritiesType(symbolInfo.getType().name());
            map.put(item.getSymbol(), symbolInfo);
        });
    }

    private void downloadSymbolList(String id, Map<String, SymbolNameResponse.Item> map, String nextKey) {
        SymbolNameResponse response = lotteApiService.get(id, appConf.getApiConnection().getSymbolNames(),
                SymbolNameResponse.class, new SymbolNameRequest(nextKey), log);
        AtomicReference<String> next = new AtomicReference<>(null);
        response.getDataList().forEach(it -> {
            if (it.isHasNext()) {
                next.set(it.getNextKey());
            } else {
                next.set(null);
            }
            it.getList().forEach(item -> map.put(item.getCode(), item));
        });
        if (next.get() != null) {
            downloadSymbolList(id, map, next.get());
        }
    }


    private void downloadSymbolInfo(String id, Map<String, SymbolInfo> symbolInfoMap, Map<String, SymbolNameResponse.Item> symbols) {
        List<String> codes = new ArrayList<>();
        Map<String, SymbolBidAskResponse> bidAskMap = new HashMap<>();
        symbols.forEach((code, it) -> {
            SymbolBidAskRequest req = new SymbolBidAskRequest(code);
            SymbolBidAskResponse response = lotteApiService.get(id, appConf.getApiConnection().getBestBidAsks(),
                    SymbolBidAskResponse.class, req, log);
            bidAskMap.put(code, response);
        });
        symbols.forEach((code, it) -> {
            if (it.getType() == SymbolType.bond || it.getType() == SymbolType.fund) {
                return;
            }
            codes.add(it.getCode());
            querySymbolInfo(id, symbolInfoMap, symbols, bidAskMap, codes, false);
        });
        querySymbolInfo(id, symbolInfoMap, symbols, bidAskMap, codes, true);
    }

    private void querySymbolInfo(String id, Map<String, SymbolInfo> symbolInfoMap, Map<String, SymbolNameResponse.Item> symbols, Map<String, SymbolBidAskResponse> bidAskMap, List<String> codes, boolean force) {
        if (codes.size() >= 20 || force) {
            SymbolPriceRequest req = SymbolPriceRequest.builder().symbols(codes).build();
            SymbolPriceResponse response = lotteApiService.get(id, appConf.getApiConnection().getSymbolPrices(),
                    SymbolPriceResponse.class, req, log);
            codes.clear();
            response.getDataList().forEach(l ->
                    l.getList().forEach(item -> {
                        try {
                            SymbolBidAskResponse.Item bidAsk = null;
                            SymbolBidAskResponse bidAskResponse = bidAskMap.get(item.getCode());
                            if (bidAskResponse != null && !bidAskResponse.getDataList().isEmpty()) {
                                bidAsk = bidAskResponse.getDataList().get(0);
                            }
                            symbolInfoMap.put(item.getCode(), this.merge(symbols.get(item.getCode()), item, bidAsk));
                        } catch (GeneralException e) {
                            log.error("{} symbolName is null {} {}", id, item, item.getCode());
                        }
                    }));
        }
    }

    private String reformatTime(String lotteTime) {
        return DefaultUtils.formatTime(DefaultUtils.parseZonedTime(lotteTime, "HH:mm:ss", DefaultUtils.VIETNAM_ID));
    }

    private Double parseDouble(String numberAsString, boolean throwIfError) {
        return parseDouble(numberAsString, null, throwIfError);
    }

    private Double parseDouble(String numberAsString, Double defaultValue, boolean throwIfError) {
        if (numberAsString == null || numberAsString.isEmpty()) {
            return defaultValue;
        }
        try {
            BigDecimal v = BigDecimal.valueOf(Double.parseDouble(numberAsString));
            return v.round(round4).doubleValue();
        } catch (Exception e) {
            if (throwIfError) {
                throw new RuntimeException(e);
            }
            return defaultValue;
        }
    }

//    private Long parseLong(String numberAsString, boolean throwIfError) {
//        return parseLong(numberAsString, null, throwIfError);
//    }

    private Long parseLong(String numberAsString, Long defaultValue, boolean throwIfError) {
        if (numberAsString == null || numberAsString.isEmpty()) {
            return defaultValue;
        }
        try {
            double value = Double.parseDouble(numberAsString);
            return (long) value;
        } catch (Exception e) {
            if (throwIfError) {
                throw new RuntimeException(e);
            }
            return defaultValue;
        }
    }

    private SymbolTypeEnum convertType(SymbolType type) {
        if (type == SymbolType.stock) return SymbolTypeEnum.STOCK;
        if (type == SymbolType.future) return SymbolTypeEnum.FUTURES;
        if (type == SymbolType.coveredwarrant) return SymbolTypeEnum.CW;
        if (type == SymbolType.etf) return SymbolTypeEnum.ETF;
        if (type == SymbolType.bond) return SymbolTypeEnum.BOND;
        return SymbolTypeEnum.BOND;
    }

    private SymbolInfo merge(SymbolNameResponse.Item symbolName, SymbolPriceResponse.Item symbolPrice, SymbolBidAskResponse.Item bidAskResponse) {
        if (symbolName == null) {
            throw new GeneralException();
        }
        SymbolInfo symbolInfo = new SymbolInfo();
        symbolInfo.setCode(symbolName.getCode());
        symbolInfo.setTime(reformatTime(symbolPrice.getTime()));
        symbolInfo.setDate(DefaultUtils.formatDate(ZonedDateTime.now()));
        symbolInfo.setOpen(parseDouble(symbolPrice.getOpen(), true));
        symbolInfo.setHigh(parseDouble(symbolPrice.getHigh(), true));
        symbolInfo.setLow(parseDouble(symbolPrice.getLow(), true));
        symbolInfo.setLast(parseDouble(symbolPrice.getLast(), true));
        symbolInfo.setChange(parseDouble(symbolPrice.getChange(), 0D, false));
        symbolInfo.setRate(parseDouble(symbolPrice.getChangeRate(), 0D, false));
        symbolInfo.setTradingVolume(parseLong(symbolPrice.getTradingVolume(), 0L, false));
        symbolInfo.setTradingValue(parseDouble(symbolPrice.getTradingValue(), 0D, false));
        symbolInfo.setExchange(symbolName.getExchange().name());
        if (bidAskResponse != null) symbolInfo.setMatchingVolume(parseLong(bidAskResponse.getMatchVolume(), null, true));
        if (bidAskResponse != null) {
            List<BidOfferItem> bidOfferList = new ArrayList<>();
            for (SymbolBidAskResponse.BidAskItem item : bidAskResponse.getBidAsks()) {
                BidOfferItem bidOfferItem = new BidOfferItem();
                bidOfferItem.setBidPrice(parseDouble(item.getBidPrice(), 0D, true));
                bidOfferItem.setBidVolume(parseLong(item.getBidVolume(), 0L, true));
                bidOfferItem.setOfferPrice(parseDouble(item.getOfferPrice(), 0D, true));
                bidOfferItem.setOfferVolume(parseLong(item.getOfferVolume(), 0L, true));
                if (bidOfferItem.getBidPrice() > 0 || bidOfferItem.getBidVolume() > 0 
                    || bidOfferItem.getOfferPrice() > 0 || bidOfferItem.getOfferVolume() > 0) {
                    bidOfferList.add(bidOfferItem);
                }
            }
            symbolInfo.setBidOfferList(bidOfferList);
        }
//        symbolInfo.setSequence();
        symbolInfo.setType(convertType(symbolName.getType()));
//        symbolInfo.setUpCount();
//        symbolInfo.setCeilingCount();
//        symbolInfo.setUnchangedCount();
//        symbolInfo.setDownCount();
//        symbolInfo.setFloorCount();
        symbolInfo.setHighTime(reformatTime(symbolPrice.getHighTime()));
        symbolInfo.setLowTime(reformatTime(symbolPrice.getLowTime()));
        symbolInfo.setCeilingPrice(parseDouble(symbolPrice.getCeiling(), true));
        symbolInfo.setFloorPrice(parseDouble(symbolPrice.getFloor(), true));
        symbolInfo.setReferencePrice(parseDouble(symbolPrice.getRefPrice(), true));
        symbolInfo.setAveragePrice(parseDouble(symbolPrice.getAvgPrice(), symbolInfo.getReferencePrice(), false));
        symbolInfo.setTurnoverRate(parseDouble(symbolPrice.getTurnoverRate(), false));
//        symbolInfo.setBidPrice();
//        symbolInfo.setOfferPrice();
//        symbolInfo.setBidVolume();
//        symbolInfo.setOfferVolume();
        if (bidAskResponse != null) symbolInfo.setTotalBidVolume(parseLong(bidAskResponse.getTotalBidVolume(), 0L, false));
//        symbolInfo.setTotalBidCount();
        if (bidAskResponse != null) symbolInfo.setTotalOfferVolume(parseLong(bidAskResponse.getTotalOfferVolume(), 0L, false));
//        symbolInfo.setTotalOfferCount();
        symbolInfo.setForeignerBuyVolume(parseLong(symbolPrice.getForeignBuyVol(), 0L, true));
        symbolInfo.setForeignerSellVolume(parseLong(symbolPrice.getForeignSellVol(), 0L, true));
        symbolInfo.setForeignerTotalRoom(parseLong(symbolPrice.getForeignTotalRoom(), 0L, true));
        symbolInfo.setForeignerCurrentRoom(parseLong(symbolPrice.getForeignCurrRoom(), 0L, true));
//        symbolInfo.setMatchedBy();
//        symbolInfo.setCeilingFloorEqual();
//        symbolInfo.setBasis();
//        symbolInfo.setMBasis();
//        symbolInfo.setTBasis();
//        symbolInfo.setTPrice();
//        symbolInfo.setDisparity();
//        symbolInfo.setDisparityRate();
//        symbolInfo.setOpenInterest();
        if (bidAskResponse != null) symbolInfo.setExpectedPrice(parseDouble(bidAskResponse.getExpectedPrice(), null, false));
//        symbolInfo.setExpectedVolume();
//        symbolInfo.setExpectedChange();
//        symbolInfo.setExpectedRate();
//        symbolInfo.setSession();
//        symbolInfo.setDiffBidOffer();
//        symbolInfo.setSecCode();
        symbolInfo.setName(symbolName.getVietnameseName());
        symbolInfo.setNameEn(symbolName.getEnglishName());
        symbolInfo.setMarketType(symbolInfo.getExchange());
        symbolInfo.setMarketName(symbolInfo.getExchange());
//        symbolInfo.setSecuritiesType();
//        symbolInfo.setPrevClosePrice();
//        symbolInfo.setBidofferTime();
//        symbolInfo.setOddlotBidofferTime();
//        symbolInfo.setUpdatedAt();
//        symbolInfo.setCreatedAt();
        symbolInfo.setListedQuantity(parseLong(symbolPrice.getListedQuantity(), 0L, false));
//        symbolInfo.setIndustry();
        symbolInfo.setPtTradingVolume(parseLong(symbolPrice.getPtVolume(), 0L, false));
        symbolInfo.setPtTradingValue(parseDouble(symbolPrice.getPtValue(), 0D, false));
//        symbolInfo.setPriorTradingVolume();
//        symbolInfo.setRights();
        symbolInfo.setParValue(symbolInfo.getParValue());
//        symbolInfo.setForeignerBuyValue();
//        symbolInfo.setForeignerSellValue();
//        symbolInfo.setFixSecurityType();
//        symbolInfo.setCfiCode();
//        symbolInfo.setCurrency();
        symbolInfo.setSecurityExchange(symbolName.getExchange().name());
//        symbolInfo.setRoundLot();
//        symbolInfo.setMinTradeVolume();
//        symbolInfo.setContractMultiplier();
//        symbolInfo.setIssuerName();
//        symbolInfo.setExercisePrice();
//        symbolInfo.setExerciseRatio();
//        symbolInfo.setExerciseRatioValue();
//        symbolInfo.setBreakEven();
//        symbolInfo.setCwPremium();
//        symbolInfo.setImpliedVolatility();
//        symbolInfo.setParity();
//        symbolInfo.setDelta();
//        symbolInfo.setGearingRt();
//        symbolInfo.setCapitalFulcrumPoint();
        if (symbolName.getType() == SymbolType.coveredwarrant) {
            symbolInfo.setUnderlyingSymbol(symbolName.getCode().substring(1, 4));
        }
//        symbolInfo.setUnderlyingPrice();
//        symbolInfo.setUnderlyingChange();
//        symbolInfo.setUnderlyingRate();
//        symbolInfo.setLastTradingDate();
//        symbolInfo.setMaturityDate();
        symbolInfo.setControlCode(symbolPrice.getControlCode());
//        symbolInfo.setChangeOfTotalBidVolume();
//        symbolInfo.setChangeOfTotalOfferVolume();
//        symbolInfo.setRefCode();
//        symbolInfo.setFirstTradingDate();
//        symbolInfo.setPriorVolume();
//        symbolInfo.setBaseCode();
//        symbolInfo.setBaseCodeSecuritiesType();
//        symbolInfo.setOpenInterestChange();
//        symbolInfo.setNormalForeignerBuyVolume();
//        symbolInfo.setNormalForeignerBuyValue();
//        symbolInfo.setNormalForeignerSellVolume();
//        symbolInfo.setNormalForeignerSellValue();
//        symbolInfo.setPtForeignerTotalBuyVolume();
//        symbolInfo.setPtForeignerTotalBuyValue();
//        symbolInfo.setPtForeignerTotalSellVolume();
//        symbolInfo.setPtForeignerTotalSellValue();
//        symbolInfo.setRemainDate();
//        symbolInfo.setTheoryPrice();
//        symbolInfo.setTheoryBasis();
//        symbolInfo.setMarketBasis();
//        symbolInfo.setDisparate();
//        symbolInfo.setDisparateRate();
//        symbolInfo.setIndexType();
//        symbolInfo.setIsHighlight();
//        symbolInfo.setTradeCount();
//        symbolInfo.setUnTradeCount();
//        symbolInfo.setQuoteSequence();
//        symbolInfo.setBidAskSequence();
//        symbolInfo.setUpdatedBy();
        if (bidAskResponse != null) symbolInfo.setSessions(bidAskResponse.getControlCode());
//        symbolInfo.setEstimatedData();
        symbolInfo.setMarketName(symbolName.getExchange().name());
        symbolInfo.setSecuritiesType(symbolInfo.getType().name());
//        symbolInfo.setINAV();
//        symbolInfo.setIIndexValue();
        HighLowYearItem item = new HighLowYearItem();
        item.setHighPrice(parseDouble(symbolPrice.getHigh52(), 0D, false));
        item.setLowPrice(parseDouble(symbolPrice.getLow52(), 0D, false));
        if (item.getHighPrice() > 0 || item.getLowPrice() > 0) {
            symbolInfo.setHighLowYearData(Collections.singletonList(item));
        }
        return symbolInfo;
    }

    private CompletableFuture<List<SymbolNameResponse.Item>> downloadSymbol(String id, int index) {
        log.info("{} downloadSymbol {} times", id, index);
        if (index >= Constants.MAX_RETRY) {
            log.error("{} retry getTblFiles exceeded {} times Stop", id, Constants.MAX_RETRY);
            return CompletableUtil.exception(new RuntimeException("retry getTblFiles exceeded {} times Stop"));
        }
        try {
            log.info("========================= {} START DOWNLOAD SYMBOL INFO {}============================", id, index);
            if (holidayService.isHoliday()) {
                log.info("========== {} TODAY IS HOLIDAY OR WEEKEND - END getTblFiles =======", id);
                return CompletableFuture.completedFuture(null);
            }

            log.info("{} download tbl from hts server master", id);
            Map<String, SymbolNameResponse.Item> nonIndexMap = new HashMap<>();
            this.downloadSymbolList(id, nonIndexMap, null);
            Map<String, IndexListResponse.Item> indexMap = new HashMap<>();
            this.downloadIndexList(id, indexMap, null);

//            CompletableTaskPool pool = new CompletableTaskPool()
            Map<String, SymbolInfo> mapSymbol = new HashMap<>();
            this.downloadSymbolInfo(id, mapSymbol, nonIndexMap);
            log.info("{} finish query symbol info {} {}", id, nonIndexMap.size(), mapSymbol.size());
            log.info("{} start query index", id);
            this.downloadIndexInfo(id, mapSymbol, indexMap);
            log.info("{} finish query index info {} {}", id, indexMap.size(), mapSymbol.size());

            List<SymbolInfo> stockList = new ArrayList<>();
            List<SymbolInfo> indexList = new ArrayList<>();
            List<SymbolInfo> futuresList = new ArrayList<>();
            List<SymbolInfo> bondList = new ArrayList<>();
            List<SymbolInfo> cwList = new ArrayList<>();
            List<SymbolInfo> allSymbols = new ArrayList<>();
            mapSymbol.forEach((code, symbol) -> {
                boolean adding = true;
                if (symbol.getType().equals(SymbolTypeEnum.INDEX)) {
                    indexList.add(symbol);
                } else if (symbol.getType().equals(SymbolTypeEnum.STOCK)) {
                    stockList.add(symbol);
                } else if (symbol.getType().equals(SymbolTypeEnum.FUTURES)) {
                    futuresList.add(symbol);
                } else if (symbol.getType().equals(SymbolTypeEnum.BOND)) {
                    if (appConf.isEnableBond()) {
                        bondList.add(symbol);
                    } else {
                        adding = false;
                    }
                } else if (symbol.getType().equals(SymbolTypeEnum.CW)) {
                    cwList.add(symbol);
                }
                if (adding) {
                    allSymbols.add(symbol);
                }
            });
            log.info("{} stockList: {} _ indexList: {} _ futuresList: {} _ cwList: {} _ total: {}", id,
                    stockList.size(), indexList.size(), futuresList.size(), cwList.size(), allSymbols.size());
            if (allSymbols.size() < appConf.getInitThresholdSize()) {
                throw new IllegalStateException(id + " Not enough symbols:" + allSymbols.size() + " while need at least: " + appConf.getInitThresholdSize());
            }
            if (appConf.isEnableInitMarket()) {
                this.marketInit.init(allSymbols);
            } else {
                String groupId = String.format("%d-%s", System.currentTimeMillis(), id);
                this.marketInit.sendSymbolInfoUpdate(groupId, appConf.getTopics().getSymbolInfoUpdate(), allSymbols, command -> command.setCleanAll(true));
                log.info("========================= {} FINISH DOWNLOAD SYMBOL INFO {}============================", id, index);
            }
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("{} error while downloadSymbol", id, e);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ignore) {
            }
            return this.downloadSymbol(id, index + 1);
        }
    }

}