package com.techx.tradex.realtime.services;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.redis.ListQuoteMeta;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.redis.QuotePartition;
import com.difisoft.market.common.utils.ConvertUtils;
import com.difisoft.market.common.utils.ThreadHandler;
import com.difisoft.market.model.common.HighLowYearItem;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.*;
import com.difisoft.market.model.v2.realtime.ExtraUpdate;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.responses.Response;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.DoubleOp;
import com.difisoft.model.utils.LongOp;
import com.fasterxml.jackson.core.type.TypeReference;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.constants.BidAskType;
import com.techx.tradex.realtime.model.request.FloorOrCeilingRequest;
import com.techx.tradex.realtime.model.request.QuoteRecover;
import com.techx.tradex.realtime.model.response.NotificationResponse;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class QuoteService {
    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);

    private final MarketRedisDao marketRedisDao;
    private final CacheService cacheService;
    private final KafkaProducer kafkaProducer;
    private final CommonService commonService;
    private final AppConf appConf;
    private Date startCurrentDate = DateUtils.truncate(new Date(), Calendar.DATE);
    private String dateStr;
    private Long setDateStrAt;
    private int recoverMinuteCycle = 0;

    @Autowired
    public QuoteService(MarketRedisDao marketRedisDao, CacheService cacheService, KafkaProducer kafkaProducer,
                        CommonService commonService, AppConf appConf) {
        this.marketRedisDao = marketRedisDao;
        this.cacheService = cacheService;
        this.kafkaProducer = kafkaProducer;
        this.commonService = commonService;
        this.appConf = appConf;
    }

    private ListQuoteMeta getQuoteMeta(String code, AtomicReference<QuotePartition> defaultPartition) {
        ListQuoteMeta meta = cacheService.getQuoteMetaMap().computeIfAbsent(code, s -> {
            ListQuoteMeta tmp = marketRedisDao.getQuoteMeta(s);
            if (tmp != null && !tmp.isEmpty()) {
                defaultPartition.set(tmp.get(0));
                defaultPartition.get().getQuotes().addAll(marketRedisDao.getAllSymbolQuote(s));
            }
            return tmp == null ? new ListQuoteMeta() : tmp;
        });
        if (meta.isEmpty()) {
            defaultPartition.set(new QuotePartition(-1));
            meta.add(defaultPartition.get());
        } else {
            defaultPartition.set(meta.get(0));
        }
        return meta;
    }

    public CompletableFuture<Void> mergeWrongOrderQuotes(String code, RequestContext<String> ctx) {
        AtomicInteger countSize = new AtomicInteger(0);
        this.handleWrongOrderQuote(code, countSize);
        this.recoverMinute(ctx.getId(), code);
        return CompletableFuture.completedFuture(null);
    }

    public void handleWrongOrderQuote(String code, AtomicInteger countSize) {
        AtomicReference<QuotePartition> defaultPartition = new AtomicReference<>();
        ListQuoteMeta meta = getQuoteMeta(code, defaultPartition);
        Set<QuotePartition> changedPartitions = new HashSet<>();
        List<SymbolQuote> addedWrongOrderToDefaults = new ArrayList<>();
        boolean isSaveMeta = false;
        boolean isAllAddedToDefault = false;
        if (appConf.isEnableCheckOrderQuote() && appConf.isEnableSaveWrongOrderQuote()) {
            List<SymbolQuote> wrongOrderQuotes = marketRedisDao.getAllSymbolQuoteWrong(code);
            if (!wrongOrderQuotes.isEmpty()) {
                log.info("wrong orders {} size {}", code, wrongOrderQuotes.size());
                countSize.addAndGet(wrongOrderQuotes.size());
                marketRedisDao.addCodeToQuoteMinuteRecover(code, null);
            }
            wrongOrderQuotes.forEach(it -> {
                if (it == null) return;
                Long volume = it.getTradingVolume();
                if (volume == null) return;
                AtomicReference<QuotePartition> founded = new AtomicReference<>(null);
                for (int i = 0; i < meta.size(); i++) {
                    QuotePartition partition = meta.get(i == meta.size() - 1 ? 0 : i + 1); // move default partition to top search
                    founded.set(partition);
                    if (volume < partition.getToVolume()) {
                        break;
                    }
                }
                if (founded.get().getPartition() == -1) {
                    addedWrongOrderToDefaults.add(it);
                }
                if (founded.get().getQuotes().isEmpty()) { // query list quote of partition. the default is added to memory already
                    founded.get().getQuotes().addAll(marketRedisDao.getAllSymbolQuote(code, String.valueOf(founded.get().getPartition())));
                }
                founded.get().add(it);
                changedPartitions.add(founded.get());
            });
            isAllAddedToDefault = wrongOrderQuotes.size() == addedWrongOrderToDefaults.size();
        }


        for (QuotePartition partition : changedPartitions) {
            if (partition.getPartition() < 0) {
                // we will migrate this list finally
                break;
            }
            marketRedisDao.remakePartition(partition.getQuotes(), code, String.valueOf(partition.getPartition()));
        }

        // if default partition and the wrong order size that will be added to default partition >= minimum size of a partition
        // then we should migrate default to a new partition and clear default partition data
        boolean shouldMigrateDefaultPartition = (defaultPartition.get().getQuotes().size() + addedWrongOrderToDefaults.size()) >= appConf.getQuotePartitionMinimumSize();
        /*
         * if we not migrate the default partition:
         *   if all wrong are to be added to default then no need to do anything
         *   if addedWrongOrderToDefaults.size = 0 then just remove wrong order key
         *   if addedWrongOrderToDefaults.size > 0 then we need to remove wrong order key and rewrite data
         * if we migrate default partition to a new partition then we can completely remove wrong order key             *
         */
        if (shouldMigrateDefaultPartition) {
            marketRedisDao.removeWrongOrderQuote(code);
        } else {
            if (!isAllAddedToDefault) {
                marketRedisDao.removeWrongOrderQuote(code);
                if (!addedWrongOrderToDefaults.isEmpty()) {
                    marketRedisDao.addSymbolQuoteWrongOrders(addedWrongOrderToDefaults);
                }
            }
        }

        /*
         * migrate from default (partition -1) to a new partition
         * adding wrong orders to default partitions
         * copy default partitions to a new partition
         * reset default partition params
         */

        if (shouldMigrateDefaultPartition) {
            log.info("migrate from default to new partition");
            addedWrongOrderToDefaults.forEach(defaultPartition.get()::add);
            QuotePartition newPartition = null;
            while (defaultPartition.get().getQuotes().size() > appConf.getQuotePartitionMinimumSize() ) {
                newPartition = new QuotePartition(meta.size() - 1, defaultPartition.get(), appConf.getQuotePartitionMinimumSize());
                log.info("{} create new partition {} -{} - default size {}", code, newPartition.getPartition(),
                        newPartition.getQuotes().size(), defaultPartition.get().getQuotes().size());
                String partitionName = newPartition.getPartition() + "";
                for (SymbolQuote it : newPartition.getQuotes()) {
                    marketRedisDao.addSymbolQuote(it, partitionName);
                }
                meta.add(newPartition);
                isSaveMeta = true;
            }
        } else { // will save to data to default partition. otherwise the wrong count will be there forever
            log.info("merge wrong order to default partition {} {} {}", code, addedWrongOrderToDefaults.size(), defaultPartition.get().getQuotes().size());
            addedWrongOrderToDefaults.forEach(defaultPartition.get()::add);
            marketRedisDao.removeWrongOrderQuote(code);
            marketRedisDao.replaceSymbolQuotes(code, defaultPartition.get().getQuotes());
            log.info("merge wrong order to default partition {} {}", code, marketRedisDao.symbolQuoteSize(code));
        }
        if (isSaveMeta) {
            log.info("re-writing list quote of default partition {}", code);
            marketRedisDao.clearSymbolQuote(code);
            defaultPartition.get().getQuotes().forEach(marketRedisDao::addSymbolQuote);
            log.info("save quote meta {}", code);
            marketRedisDao.saveQuoteMeta(code, meta);
        }
        // clear quotes except 2 last items to reduce mem and default partition
        for (int i = 1; i < meta.size() - 2; i++) {
            meta.get(i).getQuotes().clear();
        }
    }

    public void handleWrongOrderQuote(ThreadHandler<Object> threadHandler) {
        if (!appConf.isEnableQuotePartition()) return;
        Set<String> codes = new HashSet<>(threadHandler.getItems().keySet());
        AtomicInteger countSize = new AtomicInteger(0);
        codes.forEach(code -> {
            handleWrongOrderQuote(code, countSize);
        });
        recoverMinuteCycle++;
        if (recoverMinuteCycle >= appConf.getCycleToRecoverMinute()) {
            recoverMinuteCycle = 0;
            this.recoverMinute(codes);
        }
        if (countSize.get() > 0) {
            log.info("finish handle wrong order {}", countSize.get());
        }
    }

    public void recoverMinute(String code) {
        if (!marketRedisDao.isHasQuoteRecover(code)) return;
        this.recoverMinute(null, code);
    }

    public void recoverMinute(String msgId, String code) {
        String logId = msgId == null ? "wrongOrder" : msgId;
        log.info("{} start recover minute {}", logId, code);
        AtomicReference<QuotePartition> defaultPartition = new AtomicReference<>(null);
        ListQuoteMeta meta = this.getQuoteMeta(code, defaultPartition);
        List<SymbolQuoteMinute> minutes = new ArrayList<>();
        Map<Long, SymbolQuoteMinute> mapMinutes = new HashMap<>();
        AtomicReference<SymbolQuoteMinute> current = new AtomicReference<>();
        for (int i = 0; i < meta.size(); i++) {
            QuotePartition partition = meta.get(i == meta.size() - 1 ? 0 : i + 1);
            if (partition.getPartition() == -1) {
                partition.getQuotes().forEach(q -> processEachQuoteForMinute(mapMinutes, minutes, current, q));
            } else {
                marketRedisDao.getAllSymbolQuote(code, String.valueOf(partition.getPartition())).
                        forEach(q -> processEachQuoteForMinute(mapMinutes, minutes, current, q));
            }
        }
        marketRedisDao.removeCodeToQuoteMinuteRecover(code);
        if (minutes.isEmpty()) return;
        marketRedisDao.clearSymbolQuoteMinute(code);
        marketRedisDao.addSymbolQuoteMinutes(minutes);
        log.info("{} finish recover minute {}", logId, code);
    }

    private void recoverMinute(Set<String> codes) {
        codes.forEach(this::recoverMinute);
    }

    private void processEachQuoteForMinute(Map<Long, SymbolQuoteMinute> map,
                                           List<SymbolQuoteMinute> list,
                                           AtomicReference<SymbolQuoteMinute> current,
                                           SymbolQuote quote) {
        if (quote.getDate() == null || quote.getTime() == null) {
            log.info("ignore quote because of date or time {}", quote);
            return;
        }
        Date originalDate = quote.getDate();
        if (quote.getDate() == null || quote.getDate().getYear() < new Date().getYear()) {
            quote.setDate(new Date());
        }
        getMilliseconds(quote);
        Long key = quote.getMilliseconds() - (quote.getMilliseconds() % 60000);
        SymbolQuoteMinute minute = map.get(key);
        if (minute == null) {
            if (log.isDebugEnabled()) {
                SymbolQuoteMinute prev = current.get();
                if (prev != null) {
                    log.debug("+++process new minute {} - {} - {} - {} - {}", originalDate, quote.getDate(), quote.getMilliseconds(), prev.getMilliseconds(), prev.getDate());
                } else {
                    log.debug("+++process new minute {} - {} - {}", originalDate, quote.getDate(), quote.getMilliseconds());
                }
            }
            minute = new SymbolQuoteMinute();
            ConvertUtils.fromSymbolQuote(minute, quote);
            list.add(minute);
            current.set(minute);
            map.put(key, minute);
        } else {
            log.debug("===process old minute {} - {} - {} - {} - {}", originalDate, quote.getDate(), quote.getMilliseconds(), minute.getMilliseconds(), minute.getDate());
            ConvertUtils.updateByQuote(minute, quote);
        }
    }

    public void updateQuote(SymbolQuote symbolQuote) {
        cacheService.getMapMinuteSymbolInfo().add(symbolQuote.getCode());
        if (symbolQuote.getDate() == null || symbolQuote.getDate().getYear() < new Date().getYear()) {
            symbolQuote.setDate(new Date());
        }
        // FOR odd-lot
        if (symbolQuote instanceof SymbolQuoteOddLot && cacheService.isEnableAutoData()) {
            SymbolInfoOddLot symbolInfo = (SymbolInfoOddLot) cacheService.getMapSymbolInfo().get(symbolQuote.getCode());
            if (symbolInfo != null) {
                ConvertUtils.updateByQuote(symbolInfo, symbolQuote);
                marketRedisDao.setSymbolInfoOddLot(symbolInfo);
                symbolQuote.setSequence(symbolInfo.getQuoteSequence());
            }
        }

        /*
         * if quote recover. just add to wrong order
         */
        if (symbolQuote instanceof QuoteRecover quoteRecover) {
            marketRedisDao.addSymbolQuoteWrongOrder(quoteRecover);
            return;
        }

        // FOR non odd-lot
        if (!reCalculate(symbolQuote)) {
            return;
        }

        if (symbolQuote.getTradingVolume() >= 0 && symbolQuote.getType() != SymbolTypeEnum.INDEX
                && appConf.isEnableSaveStatistic()) {
            String key = symbolQuote.getCode();
            SymbolStatistic statistic;
            statistic = cacheService.getCacheStatistics().get(key);
            if (statistic == null) {
                try {
                    statistic = marketRedisDao.getStatistic(key);
                } catch (Exception ignored) {
                }
            }
            statistic = this.updateStatistic(statistic, symbolQuote);
            marketRedisDao.setStatistic(key, statistic);
            cacheService.getCacheStatistics().put(key, statistic);
            SymbolStatistic wStatistic = this.toStatistic(statistic, symbolQuote);
            this.publish(this.appConf.getPublishV2Statistic(), wStatistic);
        }

        if (cacheService.isEnableAutoData()) {
            SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(symbolQuote.getCode());
            ForeignerDaily foreignerDaily = cacheService.getMapForeignerDaily().get(symbolQuote.getCode());
            if (symbolInfo != null) {
                // updateAdditionalData(symbolQuote);
                ConvertUtils.updateByQuote(symbolInfo, symbolQuote);
                marketRedisDao.setSymbolInfo(symbolInfo);
                symbolQuote.setSequence(symbolInfo.getQuoteSequence());
            }
            if (foreignerDaily != null) {
                foreignerDaily.updateByQuote(symbolQuote);
                foreignerDaily.setDate(symbolQuote.getDate() == null
                        ? new Date()
                        : symbolQuote.getDate());
                marketRedisDao.setForeignerDaily(foreignerDaily);
            }
            this.upsertSymbolDaily(symbolQuote, symbolInfo);
        } else {
            log.warn("Ignore Info/Daily by quote because of not enableAutoData");
        }

        SymbolQuoteMinute symbolQuoteMinute = cacheService.getCacheCurrentQuoteMinute().get(symbolQuote.getCode());
        boolean popFromRedis = false;
        if (symbolQuoteMinute == null) {
            symbolQuoteMinute = marketRedisDao.popSymbolQuoteMinute(symbolQuote.getCode());
            popFromRedis = symbolQuoteMinute != null;
            if (symbolQuoteMinute != null) cacheService.getCacheCurrentQuoteMinute().put(symbolQuote.getCode(), symbolQuoteMinute);
        }
        long compare = symbolQuoteMinute == null ? 0L : (symbolQuote.getMilliseconds() / 60000) - (symbolQuoteMinute.getMilliseconds() / 60000);
        if (symbolQuoteMinute == null || compare > 0L) {
            // when there is no current minute or minute of new quote is larger than current minute -> create new minute
            if (popFromRedis) {
                // because we pop from redis. so we need to add it back again
                marketRedisDao.addSymbolQuoteMinute(symbolQuoteMinute);
            }
            SymbolQuoteMinute newSymbolQuoteMinute = new SymbolQuoteMinute();
            cacheService.getCacheCurrentQuoteMinute().put(symbolQuote.getCode(), newSymbolQuoteMinute);
            ConvertUtils.fromSymbolQuote(newSymbolQuoteMinute, symbolQuote);
            marketRedisDao.addSymbolQuoteMinute(newSymbolQuoteMinute);
        } else if (compare == 0L) { // if minute of quote is same with current minute -> update minute
            ConvertUtils.updateByQuote(symbolQuoteMinute, symbolQuote);
            marketRedisDao.addSymbolQuoteMinute(symbolQuoteMinute);
        } else {
            // quote's minute is lower than current minute. no update. no create new -> it shouldn't be this case
            log.warn("wrong order with minute {} - {} - {} - {} {}-{} : minute {}-{}", symbolQuote.getCode(),
                    symbolQuote.getTradingVolume(), symbolQuote.getLast(), symbolQuote.getMatchingVolume(),
                    symbolQuote.getDate(), symbolQuote.getTime(),
                    symbolQuoteMinute.getDate(), symbolQuoteMinute.getTime()
            );
        }
        marketRedisDao.addSymbolQuote(symbolQuote);
        AtomicReference<QuotePartition> defaultPartition = new AtomicReference<>();
        getQuoteMeta(symbolQuote.getCode(), defaultPartition);
        defaultPartition.get().add(symbolQuote);

        if (appConf.getNotifications().getCeilingOrFloorPrice().getIsActive()) {
            triggerCeilFloorPrice(
                    symbolQuote.getCeilingPrice()
                    , symbolQuote.getFloorPrice()
                    , symbolQuote.getLast()
                    , symbolQuote.getCode()
                    , symbolQuote.getRate()
            );
        }
    }

    private SymbolStatistic toStatistic(SymbolStatistic statistic, SymbolQuote item) {
        SymbolStatistic wsStatistic = new SymbolStatistic();
        wsStatistic.setCode(statistic.getCode());
        wsStatistic.setTradingVolume(statistic.getTradingVolume());
        wsStatistic.setTime(statistic.getTime());
        wsStatistic.setType(statistic.getType());
        wsStatistic.setTotalBuyVolume(statistic.getTotalBuyVolume());
        wsStatistic.setTotalSellVolume(statistic.getTotalSellVolume());
        wsStatistic.setTotalBuyRaito(statistic.getTotalBuyRaito());
        wsStatistic.setTotalSellRaito(statistic.getTotalSellRaito());
        wsStatistic.setTotalUnkownVolume(statistic.getTotalUnkownVolume());
        wsStatistic.setTotalUnkownRaito(statistic.getTotalUnkownRaito());
        Prices price = statistic.getPrices().stream().filter(p -> p.getPrice().equals(item.getLast())).findFirst()
                .orElse(null);
        List<Prices> priceList = new ArrayList<>();
        priceList.add(price);
        wsStatistic.setPrices(priceList);
        return wsStatistic;
    }

    private SymbolStatistic updateStatistic(SymbolStatistic statistic, SymbolQuote item) {
        if (statistic == null) {
            statistic = new SymbolStatistic();
            statistic.setCode(item.getCode());
            statistic.setTime(item.getTime());
            statistic.setType(item.getType().name());
            statistic.setTradingVolume(item.getTradingVolume());
            List<Prices> priceList = new ArrayList<>();
            Prices price = new Prices();
            price.setPrice(item.getLast());
            price.setMatchedVolume(item.getMatchingVolume());
            price.setMatchedRaito((item.getMatchingVolume() / item.getTradingVolume().doubleValue()) * 100);
            this.updatePrice(price, item, statistic);
            priceList.add(price);
            statistic.setPrices(priceList);
        } else {
            statistic.setTime(item.getTime());
            statistic.setTradingVolume(item.getTradingVolume());
            List<Prices> priceList = statistic.getPrices();
            if (priceList == null) {
                priceList = new ArrayList<>();
                statistic.setPrices(priceList);
            }
            Prices price = priceList.stream().filter(p -> p.getPrice().equals(item.getLast())).findFirst()
                    .orElse(null);
            if (price == null) {
                price = new Prices();
                price.setPrice(item.getLast());
                price.setMatchedVolume(item.getMatchingVolume());
                price.setMatchedRaito((item.getMatchingVolume() / statistic.getTradingVolume().doubleValue()) * 100);
                this.updatePrice(price, item, statistic);
                priceList.add(price);
            } else {
                price.setMatchedVolume(price.getMatchedVolume() + item.getMatchingVolume());
                for (Prices p : priceList) {
                    p.setMatchedRaito((p.getMatchedVolume() / statistic.getTradingVolume().doubleValue()) * 100);
                }
                this.updatePrice(price, item, statistic);
            }
            if (statistic.getTotalBuyRaito() != null) {
                statistic.setTotalBuyRaito(
                        (statistic.getTotalBuyVolume() / statistic.getTradingVolume().doubleValue()) * 100);
            }
            if (statistic.getTotalSellRaito() != null) {
                statistic.setTotalSellRaito(
                        (statistic.getTotalSellVolume() / statistic.getTradingVolume().doubleValue()) * 100);
            }
            if (statistic.getTotalUnkownRaito() != null) {
                statistic.setTotalUnkownRaito(
                        (statistic.getTotalUnkownVolume() / statistic.getTradingVolume().doubleValue()) * 100);
            }
            if (price.getUnknowRaito() != null) {
                price.setUnknowRaito(
                        (price.getMatchedUnknowVolume() / price.getMatchedVolume().doubleValue()) * 100);
            }
            if (price.getBuyRaito() != null) {
                price.setBuyRaito((price.getMatchedBuyVolume() / price.getMatchedVolume().doubleValue()) * 100);
            }
            if (price.getSellRaito() != null) {
                price.setSellRaito((price.getMatchedSellVolume() / price.getMatchedVolume().doubleValue()) * 100);
            }
        }
        return statistic;
    }

    private void updatePrice(Prices price, SymbolQuote item, SymbolStatistic statistic) {
        if (item.getMatchedBy() == null || item.getMatchedBy().isEmpty() || item.getMatchedBy()
                .equalsIgnoreCase(BidAskType.UNKNOWN.name())) {
            price.setMatchedUnknowVolume(price.getMatchedUnknowVolume() == null ? item.getMatchingVolume()
                    : price.getMatchedUnknowVolume() + item.getMatchingVolume());
            price.setUnknowRaito((price.getMatchedUnknowVolume() / price.getMatchedVolume().doubleValue()) * 100);
            statistic.setTotalUnkownVolume(statistic.getTotalUnkownVolume() == null ? item.getMatchingVolume()
                    : statistic.getTotalUnkownVolume() + item.getMatchingVolume());
            statistic.setTotalUnkownRaito(
                    (statistic.getTotalUnkownVolume() / statistic.getTradingVolume().doubleValue()) * 100);
        } else if (item.getMatchedBy().equalsIgnoreCase(BidAskType.BID.name())) {
            price.setMatchedBuyVolume(price.getMatchedBuyVolume() == null ? item.getMatchingVolume()
                    : price.getMatchedBuyVolume() + item.getMatchingVolume());
            price.setBuyRaito((price.getMatchedBuyVolume() / price.getMatchedVolume().doubleValue()) * 100);
            statistic.setTotalBuyVolume(statistic.getTotalBuyVolume() == null ? item.getMatchingVolume()
                    : statistic.getTotalBuyVolume() + item.getMatchingVolume());
            statistic.setTotalBuyRaito(
                    (statistic.getTotalBuyVolume() / statistic.getTradingVolume().doubleValue()) * 100);
        } else if (item.getMatchedBy().equalsIgnoreCase(BidAskType.ASK.name())) {
            price.setMatchedSellVolume(price.getMatchedSellVolume() == null ? item.getMatchingVolume()
                    : price.getMatchedSellVolume() + item.getMatchingVolume());
            price.setSellRaito((price.getMatchedSellVolume() / price.getMatchedVolume().doubleValue()) * 100);
            statistic.setTotalSellVolume(statistic.getTotalSellVolume() == null ? item.getMatchingVolume()
                    : statistic.getTotalSellVolume() + item.getMatchingVolume());
            statistic.setTotalSellRaito(
                    (statistic.getTotalSellVolume() / statistic.getTradingVolume().doubleValue()) * 100);
        }
    }

    public void upsertSymbolDaily(SymbolQuote symbolQuote, SymbolInfo info) {
        SymbolDaily symbolDaily = cacheService.getMapSymbolDaily().get(symbolQuote.getCode());
        if (symbolDaily == null) {
            if (info != null) {
                symbolDaily = new SymbolDaily();
                ConvertUtils.fromSymbolInfo(symbolDaily, info);
                cacheService.getMapSymbolDaily().put(symbolQuote.getCode(), symbolDaily);
            } else
                return;
        }
        ConvertUtils.updateDailyByQuote(symbolDaily, symbolQuote);
        marketRedisDao.setSymbolDaily(symbolDaily);
    }

    protected void publish(AppConf.PublishConf publishConf, Object kafkaMessage) {
        if (this.kafkaProducer != null) {
            try {
                kafkaProducer.sendMiniMessageSafeNoResponse(publishConf.getTopic(), null, kafkaMessage);
            } catch (Exception e) {
                log.error("fail to write message to json", e);
            }
        }
    }

    public boolean reCalculate(SymbolQuote symbolQuote) {
        SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(symbolQuote.getCode());
        if (symbolInfo == null) {
            throw new GeneralException("COULD_NOT_FOUND_SYMBOL_INFO");
        }
        if (symbolQuote.getTradingVolume() < 0) {
            throw new GeneralException("TRADING_VOLUME_INVALID");
        }
        if (symbolQuote.getTradingVolume() < symbolInfo.getTradingVolume() && appConf.isEnableCheckOrderQuote()) {
            if (this.appConf.isEnableSaveWrongOrderQuote()) {
                this.marketRedisDao.addSymbolQuoteWrongOrder(symbolQuote);
            }
            log.warn("WRONG_ORDER_QUOTE: {} {} {} {}", symbolQuote.getCode(), symbolQuote.getTime(),
                    symbolQuote.getTradingVolume(), symbolInfo.getTradingVolume());
            return false;
        }

        Date currentDate = new Date();
        if (symbolQuote.getDate() == null)
            symbolQuote.setDate(currentDate);

        if (symbolQuote.getHigh() == null) {
            symbolQuote.setHigh(symbolQuote.getLast());
        }
        if (symbolQuote.getLow() == null) {
            symbolQuote.setLow(symbolQuote.getLast());
        }
        symbolQuote.setCreatedAt(currentDate);
        symbolQuote.setUpdatedAt(currentDate);
        if (dateStr == null || currentDate.getTime() - setDateStrAt > 3600000) {
            dateStr = DefaultUtils.DATE_FORMAT().format(currentDate);
            setDateStrAt = currentDate.getTime();
        }
        Date quoteDate;
        try {
            quoteDate = DefaultUtils.DATETIME_FORMAT().parse(dateStr + symbolQuote.getTime());
        } catch (ParseException e) {
            log.error("fail to parse quoteDate: {}-{}", dateStr, symbolQuote.getTime());
            return false;
        }
        getMilliseconds(symbolQuote);
        symbolQuote.setDate(quoteDate);
        symbolQuote.setId(symbolQuote.getCode() + "_" + DefaultUtils.DATE_FORMAT().format(symbolQuote.getDate()) + "_"
                + symbolQuote.getTradingVolume());

        if (symbolQuote.getType().equals(SymbolTypeEnum.STOCK)) {
            symbolQuote.setForeignerMatchBuyVolume(
                    new LongOp(symbolQuote.getForeignerBuyVolume()).minus(symbolInfo.getForeignerBuyVolume()).get());
            symbolQuote.setForeignerMatchSellVolume(
                    new LongOp(symbolQuote.getForeignerSellVolume()).minus(symbolInfo.getForeignerSellVolume()).get());
            symbolQuote.setForeignerMatchBuyValue(new DoubleOp(0.0D).plus(symbolQuote.getForeignerBuyVolume())
                    .minus(symbolQuote.getForeignerBuyVolume()).mul(symbolQuote.getLast()).get());
            symbolQuote.setForeignerMatchSellValue(new DoubleOp(0.0D).plus(symbolQuote.getForeignerSellVolume())
                    .minus(symbolQuote.getForeignerSellVolume()).mul(symbolQuote.getLast()).get());
            symbolQuote.setHoldVolume(
                    new LongOp(symbolQuote.getForeignerTotalRoom()).minus(symbolQuote.getForeignerCurrentRoom()).get());
            new DoubleOp(100D).mul(symbolQuote.getForeignerCurrentRoom()).div(symbolInfo.getForeignerTotalRoom())
                    .consumeIfNotNull(symbolQuote::setBuyAbleRatio);
            new DoubleOp(0.0D).plus(symbolQuote.getHoldVolume()).div(symbolInfo.getListedQuantity())
                    .consumeIfNotNull(symbolQuote::setHoldRatio);
        }
        if (symbolQuote.getType().equals(SymbolTypeEnum.FUTURES)) {
            symbolQuote.setRefCode(symbolInfo.getRefCode());
        }
        ExtraUpdate extraUpdate = new ExtraUpdate();
        boolean hasExtraUpdate = false;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        if (symbolInfo.getHighLowYearData() == null || symbolInfo.getHighLowYearData().isEmpty()) {
            HighLowYearItem highLowYearItem = new HighLowYearItem();
            highLowYearItem.setHighPrice(symbolQuote.getHigh());
            highLowYearItem.setDateOfHighPrice(simpleDateFormat.format(symbolQuote.getDate()));
            highLowYearItem.setLowPrice(symbolQuote.getLow());
            highLowYearItem.setDateOfLowPrice(simpleDateFormat.format(symbolQuote.getDate()));
            symbolInfo.setHighLowYearData(new ArrayList<>());
            symbolInfo.getHighLowYearData().add(highLowYearItem);
            extraUpdate.setHighLowYearData(symbolInfo.getHighLowYearData());
            hasExtraUpdate = true;
        } else {
            HighLowYearItem highLowYearItem = symbolInfo.getHighLowYearData().get(0);
            if (new DoubleOp(symbolQuote.getHigh()).gt(highLowYearItem.getHighPrice())) {
                highLowYearItem.setHighPrice(symbolQuote.getHigh());
                highLowYearItem.setDateOfHighPrice(simpleDateFormat.format(symbolQuote.getDate()));
                extraUpdate.setHighLowYearData(symbolInfo.getHighLowYearData());
                hasExtraUpdate = true;
            }
            if (new DoubleOp(highLowYearItem.getLowPrice()).gt(symbolQuote.getLow())) {
                highLowYearItem.setLowPrice(symbolQuote.getLow());
                highLowYearItem.setDateOfLowPrice(simpleDateFormat.format(symbolQuote.getDate()));
                extraUpdate.setHighLowYearData(symbolInfo.getHighLowYearData());
                hasExtraUpdate = true;
            }
        }
        if (hasExtraUpdate) {
            extraUpdate.setCode(symbolInfo.getCode());
            extraUpdate.setType(symbolInfo.getType());
            extraUpdate.setMarketType(symbolInfo.getMarketType());
            kafkaProducer.sendMiniMessageSafeNoResponse("calExtraUpdate", null, extraUpdate);
        }
        return true;
    }

    private Long getMilliseconds(SymbolQuote quote) {
        Long mill = quote.getMilliseconds();
        if (mill == null || mill <= 0) {
            mill = DefaultUtils.parseZonedTime(quote.getTime()).toInstant().toEpochMilli();
            quote.setMilliseconds(mill);
        }
        return mill;
    }

    public void triggerCeilFloorPrice(Double ceilingPrice, Double floorPrice, Double lastPrice, String symbol, Double rate) {
        String prefixLog = "market_realtime_triggerCeilFloorPrice";
        try {
            boolean isCeiling = Objects.equals(ceilingPrice, lastPrice);
            boolean isFloor = Objects.equals(floorPrice, lastPrice);
            if (isCeiling || isFloor) {
                FloorOrCeilingRequest request = new FloorOrCeilingRequest(symbol, isCeiling, lastPrice, rate);
                commonService.createKafkaRequest(
                        appConf.getTopics().getVirtualCore()
                        , "real-time-v2"
                        , "get:/api/v1/hitTheCeilingOrFloorPrice"
                        , request
                        , prefixLog
                        , new TypeReference<Response<NotificationResponse>>() {
                        }
                );
            }
        } catch (Exception ex) {
            log.error("{} error: ", prefixLog, ex);
        }
    }

    public void setStartCurrentDate() {
        this.startCurrentDate = DateUtils.truncate(new Date(), Calendar.DATE);
    }
}


