package com.techx.tradex.realtime.services;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.redis.ListQuoteMeta;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.ForeignerDailyRepository;
import com.difisoft.market.common.repository.SymbolDailyRepository;
import com.difisoft.market.common.repository.SymbolInfoOddLotRepository;
import com.difisoft.market.common.repository.SymbolInfoRepository;
import com.difisoft.market.common.repository.SymbolPreviousRepository;
import com.difisoft.market.common.utils.MongoBulkUtils;
import com.difisoft.market.model.constant.MarketTypeEnum;
import com.difisoft.market.model.v2.db.*;
import com.techx.tradex.realtime.model.request.SaveRedisToDatabaseRequest;
import com.techx.tradex.realtime.utils.DatetimeUtil;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    private final MarketRedisDao redisDao;
    private final SymbolInfoRepository symbolInfoRepository;
    private final SymbolInfoOddLotRepository symbolInfoOddLotRepository;
    private final SymbolDailyRepository symbolDailyRepository;
    private final SymbolPreviousRepository symbolPreviousRepository;
    private final ForeignerDailyRepository foreignerDailyRepository;
    private final CacheService cacheService;
    private final MongoTemplate mongoTemplate;
    private boolean pauseSaveToDb = false;

    @Autowired
    public RedisService(MarketRedisDao redisDao, SymbolInfoRepository symbolInfoRepository,
            SymbolInfoOddLotRepository symbolInfoOddLotRepository,
            SymbolDailyRepository symbolDailyRepository, ForeignerDailyRepository foreignerDailyRepository,
            CacheService cacheService, MongoTemplate mongoTemplate,
            SymbolPreviousRepository symbolPreviousRepository) {
        this.redisDao = redisDao;
        this.symbolInfoRepository = symbolInfoRepository;
        this.symbolInfoOddLotRepository = symbolInfoOddLotRepository;
        this.symbolDailyRepository = symbolDailyRepository;
        this.foreignerDailyRepository = foreignerDailyRepository;
        this.cacheService = cacheService;
        this.mongoTemplate = mongoTemplate;
        this.symbolPreviousRepository = symbolPreviousRepository;
    }

    public void reloadSymbolInfo() {
        log.info("start reset reloadSymbolInfo");
        long t1 = System.currentTimeMillis();
        List<SymbolInfo> symbolInfoList = this.symbolInfoRepository.findAll();
        List<SymbolInfoOddLot> symbolInfoOddLotList = this.symbolInfoOddLotRepository.findAll();
        log.info("load symbolInfoList from database: {}", symbolInfoList.size());
        redisDao.clearSymbolInfo();
        redisDao.clearSymbolInfoOddLot();
        log.info("finish clearSymbolInfo");

        for (SymbolInfo symbolInfo : symbolInfoList) {
            this.redisDao.setSymbolInfo(symbolInfo);
        }
        for (SymbolInfoOddLot symbolInfoOddLot : symbolInfoOddLotList) {
            this.redisDao.setSymbolInfoOddLot(symbolInfoOddLot);
        }
        log.info("finish update symbolInfo");
        long t2 = System.currentTimeMillis();
        log.info("finish reloadSymbolInfo take: {}", (t2 - t1));
        cacheService.reset();
    }

    public void reloadSymbolDaily() {
        log.info("start reset reloadSymbolDaily");
        long t1 = System.currentTimeMillis();
        Date today = new Date();
        List<SymbolDaily> symbolDailyList = this.symbolDailyRepository.findByDate(today);
        log.info("load symbolDailyList from database: {}", symbolDailyList.size());
        List<ForeignerDaily> foreignerDailyList = this.foreignerDailyRepository.findByDate(today);
        log.info("load foreignerDailyList from database: {}", foreignerDailyList.size());

        redisDao.clearSymbolDaily();
        log.info("finish clearSymbolDaily");
        redisDao.clearForeignerDaily();
        log.info("finish clearForeignerDaily");

        for (SymbolDaily symbolDaily : symbolDailyList) {
            this.redisDao.setSymbolDaily(symbolDaily);
        }
        log.info("finish update symbolDaily");
        for (ForeignerDaily foreignerDaily : foreignerDailyList) {
            this.redisDao.setForeignerDaily(foreignerDaily);
        }
        long t2 = System.currentTimeMillis();
        log.info("finish reloadSymbolDaily take: {}", (t2 - t1));
        cacheService.reset();
    }

    public void resetRedisCache() {
        log.info("start reset redisCache");
        long t1 = System.currentTimeMillis();
        removeAutoData();
        reloadSymbolDaily();
        reloadSymbolInfo();
        log.info("finish update symbolInfo");
        long t2 = System.currentTimeMillis();
        log.info("finish reset redisCache take: {}", (t2 - t1));

        cacheService.reset();
    }

    public boolean saveRedisToDatabaseJob(SaveRedisToDatabaseRequest request,
            RequestContext<SaveRedisToDatabaseRequest> ctx) {
        this.saveRedisToDatabase(request.isEnableSaveQuote(), request.isEnableSaveQuoteMinute(),
                request.isEnableSaveBidAsk());
        return true;
    }

    public void saveRedisToDatabase(boolean isSaveQuote, boolean isSaveQuoteMinute, boolean isSaveBidAsk) {
        log.info("start saveRedisToDatabase");
        if (pauseSaveToDb) {
            log.info("ignore saveRedisToDatabase because of pauseSaveToDb");
        }
        pauseSaveToDb = true;
        long t1 = System.currentTimeMillis();
        try {
            List<SymbolInfo> symbolInfoList = redisDao.getAllSymbolInfo();
            List<SymbolInfoOddLot> symbolInfoOddLotList = redisDao.getAllSymbolInfoOddLot();
            List<SymbolDaily> symbolDailyList = redisDao.getAllSymbolDaily();
            List<ForeignerDaily> foreignerDailyList = redisDao.getAllForeignerDaily();
            if (foreignerDailyList == null || foreignerDailyList.isEmpty()) {
                log.info("foreignerDailyList is null or empty");
                foreignerDailyList = new ArrayList<>();
                for (SymbolInfo symbolInfo : symbolInfoList) {
                    try {
                        ForeignerDaily foreignerDaily = new ForeignerDaily();
                        foreignerDaily.setCode(symbolInfo.getCode());
                        foreignerDaily.setSymbolType(symbolInfo.getType().name());
                        foreignerDaily.setDate(DatetimeUtil.toDate(symbolInfo.getDate()) == null
                                ? new Date()
                                : DatetimeUtil.toDate(symbolInfo.getDate()));
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
                        log.error("fail to convert from info to foreigner daily");
                    }
                }
            }
            List<MarketStatus> marketStatusList = redisDao.getAllMarketStatus();

            try {
                mongoTemplate.remove(SymbolInfo.class);
                MongoBulkUtils.updateInBulk(mongoTemplate, 200, symbolInfoList, SymbolInfo.class);
            } catch (Exception e) {
                log.error("fail to update symbol info", e);
            }
            long t2 = System.currentTimeMillis();
            log.info("save symbolInfo from redis to mongo take: {}", (t2 - t1));
            try {
                mongoTemplate.remove(SymbolInfoOddLot.class);
                MongoBulkUtils.updateInBulk(mongoTemplate, 200, symbolInfoOddLotList, SymbolInfoOddLot.class);
            } catch (Exception e) {
                log.error("fail to update symbol info", e);
            }
            long t10 = System.currentTimeMillis();
            log.info("save symbolInfoOddLot from redis to mongo  take: {}", (t10 - t1));
            try {
                MongoBulkUtils.updateInBulk(mongoTemplate, 200, symbolDailyList, SymbolDaily.class);
            } catch (Exception e) {
                log.error("fail to update symbol info", e);
            }
            long t3 = System.currentTimeMillis();
            log.info("save symbolDaily take: {}", (t3 - t2));
            try {
                String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
                for (ForeignerDaily foreignerDaily : foreignerDailyList) {
                    String date = new SimpleDateFormat("yyyyMMdd").format(foreignerDaily.getDate());
                    if(foreignerDaily.getDate() != null && date.equals(today)) {
                        foreignerDaily.setId(foreignerDaily.getCode() + "_" + new SimpleDateFormat("yyyyMMdd").format(foreignerDaily.getDate()));
                        foreignerDaily.setDate(foreignerDaily.getDate());
                    }
                }
                MongoBulkUtils.updateInBulk(mongoTemplate, 200, foreignerDailyList, ForeignerDaily.class);
            } catch (Exception e) {
                log.error("fail to update symbol info", e);
            }
            long t4 = System.currentTimeMillis();
            log.info("save foreignerDaily from redis to mongo take: {}", (t4 - t3));
            try {
                MongoBulkUtils.updateInBulk(mongoTemplate, 200, marketStatusList, MarketStatus.class);
            } catch (Exception e) {
                log.error("fail to update symbol info", e);
            }
            long t5 = System.currentTimeMillis();
            log.info("save marketStatus take: {}", (t5 - t4));
            int size = symbolInfoList.size();
            if (isSaveQuote) {
                for (int i = 0; i < size; i++) {
                    int totalSize = 0;
                    SymbolInfo symbolInfo = symbolInfoList.get(i);
                    long t01 = System.currentTimeMillis();
                    ListQuoteMeta meta = redisDao.getQuoteMeta(symbolInfo.getCode());
                    if (meta != null) {
                        for (int j = 1; j < meta.size(); j++) {
                            List<SymbolQuote> symbolQuoteList = redisDao.getAllSymbolQuote(symbolInfo.getCode(), String.valueOf(meta.get(i).getPartition()));
                            try {
                                MongoBulkUtils.updateInBulk(mongoTemplate, 200, symbolQuoteList, SymbolQuote.class);
                            } catch (Exception e) {
                                log.error("fail to update symbol info", e);
                            }
                            totalSize += symbolQuoteList.size();
                        }
                    }
                    try {
                        List<SymbolQuote> symbolQuoteList = redisDao.getAllSymbolQuote(symbolInfo.getCode());
                        MongoBulkUtils.updateInBulk(mongoTemplate, 200, symbolQuoteList, SymbolQuote.class);
                        totalSize += symbolQuoteList.size();
                    } catch (Exception e) {
                        log.error("fail to update symbol info", e);
                    }
                    long t02 = System.currentTimeMillis();
                    log.info("save quote for symbol {} _ {}/{}: take: {}", symbolInfo.getCode(), i, totalSize, (t02 - t01));
                }
            }
            if (isSaveQuoteMinute) {
                for (int i = 0; i < size; i++) {
                    SymbolInfo symbolInfo = symbolInfoList.get(i);
                    List<SymbolQuoteMinute> symbolQuoteMinuteList = redisDao
                            .getAllSymbolQuoteMinute(symbolInfo.getCode());
                    log.info("quoteMinute_ code: {} _ length: {}", symbolInfo.getCode(), symbolQuoteMinuteList.size());
                    long t01 = System.currentTimeMillis();
                    MongoBulkUtils.updateInBulk(mongoTemplate, 200, symbolQuoteMinuteList, SymbolQuoteMinute.class);
                    long t02 = System.currentTimeMillis();
                    log.info("save quoteMinute for symbol {} _ {}/{}: take: {}", symbolInfo.getCode(), i, size,
                            (t02 - t01));
                }
            }
            if (isSaveBidAsk) {
                for (int i = 0; i < size; i++) {
                    SymbolInfo symbolInfo = symbolInfoList.get(i);
                    List<BidOffer> bidOfferList = redisDao.getAllBidOffer(symbolInfo.getCode());
                    log.info("bidAsk_ code: {} _ length: {}", symbolInfo.getCode(), bidOfferList.size());
                    long t01 = System.currentTimeMillis();
                    MongoBulkUtils.updateInBulk(mongoTemplate, 200, bidOfferList, BidOffer.class);
                    long t02 = System.currentTimeMillis();
                    log.info("save bidOffer for symbol {} _ {}/{}: take: {}", symbolInfo.getCode(), i, size,
                            (t02 - t01));
                }
            }
            List<DealNotice> hnxDealNoticeList = redisDao.getAllDealNotice(MarketTypeEnum.HNX.name());
            MongoBulkUtils.updateInBulk(mongoTemplate, 200, hnxDealNoticeList, DealNotice.class);
            List<DealNotice> upcomDealNoticeList = redisDao.getAllDealNotice(MarketTypeEnum.UPCOM.name());
            MongoBulkUtils.updateInBulk(mongoTemplate, 200, upcomDealNoticeList, DealNotice.class);
            List<DealNotice> hoseDealNoticeList = redisDao.getAllDealNotice(MarketTypeEnum.HOSE.name());
            MongoBulkUtils.updateInBulk(mongoTemplate, 200, hoseDealNoticeList, DealNotice.class);
            List<Advertised> hnxAdvertisedList = redisDao.getAllAdvertised(MarketTypeEnum.HNX.name());
            MongoBulkUtils.updateInBulk(mongoTemplate, 200, hnxAdvertisedList, Advertised.class);
            List<Advertised> upcomAdvertisedList = redisDao.getAllAdvertised(MarketTypeEnum.UPCOM.name());
            MongoBulkUtils.updateInBulk(mongoTemplate, 200, upcomAdvertisedList, Advertised.class);
            List<Advertised> hoseAdvertisedList = redisDao.getAllAdvertised(MarketTypeEnum.HOSE.name());
            MongoBulkUtils.updateInBulk(mongoTemplate, 200, hoseAdvertisedList, Advertised.class);
            log.info("save dealNotice and advertise");

            updateSymbolPrevious();

        } catch (Exception ex) {
            log.error("Error while saveRedisToDatabase:", ex);
        }
        long t7 = System.currentTimeMillis();
        pauseSaveToDb = false;
        log.info("finish saveRedisToDatabase take: {}", (t7 - t1));
    }

    public void updateSymbolPrevious() {
        log.info("start updateSymbolPrevious");
        long t1 = System.currentTimeMillis();
        Date date = new Date();
        List<SymbolDaily> symbolDailyList = symbolDailyRepository.findByDate(date);
        if (symbolDailyList.isEmpty()) {
            return;
        }
        List<SymbolPrevious> symbolPreviousList = symbolPreviousRepository.findAll();
        Map<String, SymbolPrevious> symbolPreviousMap = new HashMap<>();
        symbolPreviousList.forEach(symbolPrevious -> symbolPreviousMap.put(symbolPrevious.getCode(), symbolPrevious));

        List<SymbolPrevious> newSymbolPreviousList = new ArrayList<>();
        for (int i = 0; i < symbolDailyList.size(); i++) {
            SymbolDaily symbolDaily = symbolDailyList.get(i);
            SymbolPrevious symbolPrevious = symbolPreviousMap.get(symbolDaily.getCode());
            if (symbolPrevious == null) {
                symbolPrevious = new SymbolPrevious();
                symbolPrevious.setCode(symbolDaily.getCode());
                symbolPrevious.setClose(symbolDaily.getLast());
                symbolPrevious.setLastTradingDate(symbolDaily.getDate());
                symbolPrevious.setMarketType(symbolDaily.getMarketType());
                symbolPrevious.setType(symbolDaily.getType());
                symbolPrevious.setRefCode(symbolDaily.getRefCode());
                symbolPrevious.setNote("create new");
                symbolPrevious.setCreatedAt(new Date());
                symbolPrevious.setUpdatedAt(new Date());
            } else {
                if (DateUtils.truncatedCompareTo(symbolPrevious.getLastTradingDate(), symbolDaily.getDate(),
                        Calendar.MONTH) == 0) {
                    symbolPrevious.setClose(symbolDaily.getLast());
                    symbolPrevious.setUpdatedAt(new Date());
                    symbolPrevious.setNote("same date -> just update close");
                } else {
                    symbolPrevious.setPreviousClose(symbolPrevious.getClose());
                    symbolPrevious.setPreviousTradingDate(symbolPrevious.getLastTradingDate());
                    symbolPrevious.setClose(symbolDaily.getLast());
                    symbolPrevious.setLastTradingDate(symbolDaily.getDate());
                    symbolPrevious.setUpdatedAt(new Date());
                    symbolPrevious.setNote("different date -> update close/previousClose");
                }
            }
            newSymbolPreviousList.add(symbolPrevious);
        }
        MongoBulkUtils.updateInBulk(mongoTemplate, 200, newSymbolPreviousList, SymbolPrevious.class);
        long t2 = System.currentTimeMillis();
        log.info("end updateSymbolPrevious, take: {} ms", (t2 - t1));
    }

    public void removeAutoData() {
        log.info("start removeAutoData");
        Set<String> keys = redisDao.clearAllQuoteMinute();
        log.info("clear all quote minutes. {} keys is deleted", keys.size());
        keys = redisDao.clearAllSymbolQuote();
        log.info("clear all quotes. {} keys is deleted", keys.size());
        keys = redisDao.clearAllSymbolQuoteMeta();
        log.info("clear all quoteMetas. {} keys is deleted", keys.size());
        keys = redisDao.clearAllSymbolQuoteWrongOrder();
        log.info("clear all quote Wrong orders. {} keys is deleted", keys.size());
        keys = redisDao.clearAllSymbolQuoteRecoverMinute();
        log.info("clear all quote recover minute. {} keys is deleted", keys.size());
        keys = redisDao.clearAllBidOffer();
        log.info("clear all bid offers. {} keys is deleted", keys.size());

        keys = redisDao.clearAllDealNotice();
        log.info("clear all deals. {} keys is deleted", keys.size());
        keys = redisDao.clearAllAdvertised();
        log.info("clear all advertised. {} keys is deleted", keys.size());
        redisDao.clearMarketStatistic();
        log.info("finish clearSymbolStastic");

        log.info("end removeAutoData");
    }

    public void refreshSymbolInfo() {
        log.info("start refreshSymbolInfo");
        // reset sequence and clear bidOfferList
        for (SymbolInfo symbolInfo : redisDao.getAllSymbolInfo()) {
            symbolInfo.setSequence(0L);
            symbolInfo.setMatchingVolume(0L);
            symbolInfo.setMatchedBy(null);
            symbolInfo.setBidOfferList(null);
            symbolInfo.setOddlotBidOfferList(null);
            symbolInfo.setUpdatedBy("Job Realtime refreshSymbolInfo");
            this.redisDao.setSymbolInfo(symbolInfo);
        }
        cacheService.reset();
        log.info("end refreshSymbolInfo");
    }

    // when receiving new extraQuote/ symbolQuote, symbolDaily will be upserted to
    // redis
    public void clearOldSymbolDaily() {
        log.info("clear old symbol daily");
        this.redisDao.clearSymbolDaily();
        this.cacheService.getMapSymbolDaily().clear();
    }

}
