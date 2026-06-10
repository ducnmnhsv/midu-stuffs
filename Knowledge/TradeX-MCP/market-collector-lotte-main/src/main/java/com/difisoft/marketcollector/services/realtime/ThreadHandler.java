package com.difisoft.marketcollector.services.realtime;

import com.difisoft.htsconnection.socket.message.AutoRcv;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.market.model.v2.realtime.StockExtra;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.realtime.*;
import com.difisoft.marketcollector.services.CacheService;
import com.difisoft.marketcollector.services.RequestSender;
import com.difisoft.marketcollector.utils.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class ThreadHandler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ThreadHandler.class);
    LinkedBlockingQueue<Data> queue;
    private final AppConf appConf;
    private final CacheService cacheService;
    private final RequestSender requestSender;

    public ThreadHandler(
            LinkedBlockingQueue<Data> queue,
            AppConf appConf,
            CacheService cacheService,
            RequestSender requestSender
    ) {
        this.queue = queue;
        this.appConf = appConf;
        this.cacheService = cacheService;
        this.requestSender = requestSender;
    }

    @Override
    public void run() {
        int continueExceptionOnTaking = 0;
        while (true) {
            Data packet;
            try {
                packet = queue.take();
            } catch (InterruptedException ie) {
                log.error("got interrupted exception");
                throw new RuntimeException(ie);
            } catch (Exception e) {
                if (continueExceptionOnTaking % 1000 == 0) {
                    log.error("fail to take packet", e);
                    continueExceptionOnTaking = 0;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie2) {
                    // swallow
                }
                continueExceptionOnTaking++;
                continue;
            }
            try {
                this.handle(packet);
            } catch (Exception e) {
                log.error("fail to handle packet {}", packet, e);
            }
        }
    }

    public void handle(Data data) {
        AutoRcv item = data.item;
        Class<?> clazz = data.srcClazz;
        Class<? extends TransformData<AutoRcv>> destClass = data.destClass;
        try {
            TransformData<AutoRcv> destInstance = destClass.newInstance();
            destInstance = destInstance.from(item);
            try {
                destInstance.validate();
                destInstance.formatTime();
                destInstance.formatRefCode(cacheService);
                destInstance.parseStatus(appConf.getStatusMap());
                destInstance.setSendOut(clazz.getSimpleName());
                if (destClass.equals(IndexUpdateData.class)) {
                    handleAutoData((IndexUpdateData) destInstance.toRealObject());
                } else if (destClass.equals(FuturesUpdateData.class)) {
                    FuturesUpdateData futuresUpdateData = (FuturesUpdateData) destInstance.toRealObject();
                    handleAutoData(futuresUpdateData);
                } else if (destClass.equals(StockUpdateData.class)) {
                    StockUpdateData stockUpdateData = (StockUpdateData) destInstance.toRealObject();
                    handleAutoData(stockUpdateData);
                } else if (destClass.equals(BidOfferData.class)) {
                    BidOfferData bidOfferData = (BidOfferData) destInstance.toRealObject();
                    handleAutoData(bidOfferData);
                } else if (destClass.equals(FuturesBidOfferData.class)) {
                    FuturesBidOfferData bidOfferData = (FuturesBidOfferData) destInstance.toRealObject();
                    handleAutoData(bidOfferData);
                } else if (destClass.equals(AdvertisedData.class)) {
                    handleAutoData((AdvertisedData) destInstance.toRealObject());
                } else if (destClass.equals(DealNoticeData.class)) {
                    DealNoticeData dealNoticeData = (DealNoticeData) destInstance.toRealObject();
                    handleAutoData(dealNoticeData);
                } else if (destClass.equals(MarketStatusData.class)) {
                    kafkaPublishRealtime("marketStatus", destInstance);
                } else {
                    log.info("todo check class: {}, {}", destInstance, clazz);
                    kafkaPublishRealtime("unknownType", destInstance);
                }
            } catch (TransformData.IgnoreException e) {
                log.error("ignore realtime packet {} with error: {}", item, e);
            }
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("fail to init item of class {}", destClass);
        }
    }

    private void handleAutoData(DealNoticeData dealNoticeData) {
        SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(dealNoticeData.getCode());
        if (symbolInfo == null) {
            log.error("handle DealNoticeData, not found symbolInfo {} in cache", dealNoticeData.getCode());
            return;
        }
        Long ptMatchingVolume = dealNoticeData.getMatchVolume();
        Double ptMatchingValue = dealNoticeData.getMatchValue() * ptMatchingVolume;

        dealNoticeData.setPtValue(symbolInfo.getPtTradingValue() + ptMatchingValue);
        dealNoticeData.setPtVolume(symbolInfo.getPtTradingVolume() + ptMatchingVolume);
        dealNoticeData.setMarketType(symbolInfo.getMarketType());
        kafkaPublishRealtime("dealNoticeUpdate", dealNoticeData);

        ExtraUpdate extraUpdate = ExtraUpdate.fromDealNotice(dealNoticeData);
        kafkaPublishRealtime("extraUpdate", extraUpdate);
    }

    private void handleAutoData(AdvertisedData advertisedData) {
        SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(advertisedData.getCode());
        if (symbolInfo == null) {
            log.error("handle advertisedData, not found symbolInfo {} in cache", advertisedData.getCode());
            return;
        }
        advertisedData.setMarketType(symbolInfo.getMarketType());
        kafkaPublishRealtime("advertisedUpdate", advertisedData);
    }

    private void handleAutoData(IndexUpdateData indexUpdateData) {
        SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(indexUpdateData.getCode());
        if (symbolInfo == null) {
            log.error("not found symbolInfo {} in cache", indexUpdateData.getCode());
            return;
        }
        symbolInfo.setLast(indexUpdateData.getLast());
        double lastTradingVolume = symbolInfo.getTradingVolume() == null ? 0D : symbolInfo.getTradingVolume();
        double lastTradingValue = symbolInfo.getTradingValue() == null ? 0D : symbolInfo.getTradingValue();
        if (indexUpdateData.getTradingVolume() <= lastTradingVolume
                && indexUpdateData.getTradingValue() <= lastTradingValue) {
            log.warn("Ignore index quote {} by lastTradingVolume: {} _ tradingVolume: {} _ lastTradingValue: {}, tradingValue: {} ",
                    symbolInfo.getCode(), lastTradingVolume, indexUpdateData.getTradingVolume(), lastTradingValue, indexUpdateData.getTradingValue());
            if (appConf.isEnableIgnoreQuote()) {
                return;
            }
        }
        symbolInfo.setTradingVolume(indexUpdateData.getTradingVolume());
        symbolInfo.setTradingValue((double) indexUpdateData.getTradingValue());
        if (indexUpdateData.getCode().equals("VN30")) {
            log.info("handle to send basis:");
            ArrayList<String> codeList = (ArrayList<String>) new ArrayList<>(cacheService.getMapSymbolInfo()
                    .keySet())
                    .stream()
                    .filter(code -> code.startsWith("VN30F"))
                    .collect(Collectors.toList());
            codeList.forEach(code -> {
                ExtraUpdate extraUpdate = new ExtraUpdate();
                SymbolInfo future = cacheService.getMapSymbolInfo().get(code);
                extraUpdate.setCode(future.getCode());
                future.setBasis(NumberUtil.round2Decimal(future.getLast() - indexUpdateData.getLast()));
                extraUpdate.setBasis(future.getBasis());
                log.info("send basis: index: {}, future: {}, extraUpdate: {}", indexUpdateData.getLast(), future.getLast(), extraUpdate);
                kafkaPublishRealtime("extraUpdate", extraUpdate);
            });
        }
        kafkaPublishRealtime("quoteUpdate", indexUpdateData);
    }

    private void handleAutoData(StockUpdateData stockUpdateData) {
        SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(stockUpdateData.getCode());
        if (symbolInfo == null) {
            log.error("not found symbolInfo {} in cache", stockUpdateData.getCode());
            return;
        }
        long lastTradingVolume = symbolInfo.getTradingVolume() == null ? 0L : symbolInfo.getTradingVolume();
        long tradingVolume = stockUpdateData.getTradingVolume();
        long matchingVolume = stockUpdateData.getMatchingVolume();
        if (matchingVolume == 0) {
            StockExtra extra = new StockExtra();
            extra.setCode(symbolInfo.getCode());
            extra.setType(symbolInfo.getType());
            extra.setExchange(symbolInfo.getExchange());
            extra.setTime(stockUpdateData.getTime());
            extra.setTradingVolume(stockUpdateData.getTradingVolume());
            extra.setLast(NumberUtil.round2Decimal(stockUpdateData.getLast()));
            extra.setOpen(NumberUtil.round2Decimal(stockUpdateData.getOpen()));
            extra.setHigh(NumberUtil.round2Decimal(stockUpdateData.getHigh()));
            extra.setLow(NumberUtil.round2Decimal(stockUpdateData.getLow()));
            extra.setAveragePrice(NumberUtil.round2Decimal(stockUpdateData.getAveragePrice()));
            kafkaPublishRealtime("extraUpdate", extra);
            return;
        }
        if (symbolInfo.getType() == SymbolTypeEnum.CW && symbolInfo.getExerciseRatioValue() != null && symbolInfo.getExercisePrice() != null) {
            double breakEven = stockUpdateData.getLast() * symbolInfo.getExerciseRatioValue() + symbolInfo.getExercisePrice();
            symbolInfo.setBreakEven(breakEven);
            stockUpdateData.setBreakEven(breakEven);
            stockUpdateData.setType(SymbolTypeEnum.CW.name());
        }
        if (lastTradingVolume == 0 || (tradingVolume - matchingVolume == lastTradingVolume)) {
            symbolInfo.setTradingVolume(tradingVolume);
            symbolInfo.setLast((double) stockUpdateData.getLast());
        }
        if ((lastTradingVolume - tradingVolume == 0) || (tradingVolume - matchingVolume < lastTradingVolume)) {
            log.warn("Ignore stock quote {} by lastTradingVolume: {} _ tradingVolume: {} _ matchingVolume: {}, destInstance: {} ",
                    symbolInfo.getCode(), lastTradingVolume, tradingVolume, matchingVolume, stockUpdateData);
            if (appConf.isEnableIgnoreQuote()) {
                return;
            }
        } else if (tradingVolume - matchingVolume > lastTradingVolume) {
            log.error("Wrong order, lastTradingVolume: {} _ stockQuote: {} -> add To Queue", lastTradingVolume, stockUpdateData);
            if (appConf.isEnableIgnoreQuote()) {
                return;
            }
        }
        kafkaPublishRealtime("quoteUpdate", stockUpdateData);
    }

    private void handleAutoData(FuturesUpdateData futuresUpdateData) {
        SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(futuresUpdateData.getCode());
        if (symbolInfo == null) {
            log.error("not found symbolInfo {} in cache", futuresUpdateData.getCode());
            return;
        }
        long lastTradingVolume = symbolInfo.getTradingVolume();
        long tradingVolume = futuresUpdateData.getTradingVolume();
        long matchingVolume = futuresUpdateData.getMatchingVolume();
        if (futuresUpdateData.getCode().startsWith("VN30")) {
            SymbolInfo vn30 = cacheService.getMapSymbolInfo().get("VN30");
            if (vn30 != null) {
                futuresUpdateData.setBasis(NumberUtil.round2Decimal(futuresUpdateData.getLast() - vn30.getLast()));
            }
        }
        if (lastTradingVolume == 0 || (tradingVolume - matchingVolume == lastTradingVolume)) {
            symbolInfo.setTradingVolume(tradingVolume);
            symbolInfo.setLast((double) futuresUpdateData.getLast());
        }
        if ((lastTradingVolume - tradingVolume == 0) || (tradingVolume - matchingVolume < lastTradingVolume)) {
            log.warn("Ignore futures quote {} by lastTradingVolume: {} _ tradingVolume: {} _ matchingVolume: {}, destInstance: {} ",
                    symbolInfo.getCode(), lastTradingVolume, tradingVolume, matchingVolume, futuresUpdateData);
            if (appConf.isEnableIgnoreQuote()) {
                return;
            }
        } else if ((tradingVolume - matchingVolume > lastTradingVolume)) {
            log.error("Wrong order, lastTradingVolume: {} _ futuresQuote: {} -> add To Queue", lastTradingVolume, futuresUpdateData);
            if (appConf.isEnableIgnoreQuote()) {
                return;
            }
        }
        kafkaPublishRealtime("quoteUpdate", futuresUpdateData);
    }

    private void handleAutoData(BidOfferData bidOfferData) {
        Double expectedPrice = bidOfferData.getExpectedPrice();
        if (expectedPrice != null) {
            Double referencePrice = cacheService.getMapSymbolInfo().get(bidOfferData.getCode()).getReferencePrice();
            if (referencePrice != null && referencePrice > 0) {
                double expectedChange = expectedPrice - referencePrice;
                double expectedRate = (expectedChange / referencePrice) * 100;
                bidOfferData.setExpectedChange(NumberUtil.round2Decimal(expectedChange));
                bidOfferData.setExpectedRate(NumberUtil.round2Decimal(expectedRate));
            }
        }
        kafkaPublishRealtime("bidOfferUpdate", bidOfferData);
    }

    private void handleAutoData(FuturesBidOfferData bidOfferData) {
        Double expectedPrice = bidOfferData.getExpectedPrice();
        if (expectedPrice != null) {
            Double referencePrice = cacheService.getMapSymbolInfo().get(bidOfferData.getCode()).getReferencePrice();
            if (referencePrice != null && referencePrice > 0) {
                double expectedChange = expectedPrice - referencePrice;
                double expectedRate = (expectedChange / referencePrice) * 100;
                bidOfferData.setExpectedChange(NumberUtil.round2Decimal(expectedChange));
                bidOfferData.setExpectedRate(NumberUtil.round2Decimal(expectedRate));
            }
        }
        kafkaPublishRealtime("bidOfferUpdate", bidOfferData);
    }

    public void kafkaPublishRealtime(String topic, Object data) {
        if (appConf.isLogRealtimePacket()) {
            this.requestSender.sendMessageSafeAndLog(topic, "Update", data);
        } else {
            this.requestSender.sendMessageSafe(topic, "Update", data);
        }
    }

    public static class Data {
        AutoRcv item;
        Class<?> srcClazz;
        Class<? extends TransformData<AutoRcv>> destClass;

        public Data(AutoRcv item, Class<?> outClazz, Class<? extends TransformData<AutoRcv>> destClass) {
            this.item = item;
            this.srcClazz = outClazz;
            this.destClass = destClass;
        }

        public AutoRcv getItem() {
            return item;
        }
    }
}
