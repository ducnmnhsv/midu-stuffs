package com.difisoft.marketcollector.services;

import com.difisoft.htsconnection.socket.message.receive.*;
import com.difisoft.htsconnection.socket.message.send.*;
import com.difisoft.htsconnection.socket.nonblocking.BaseHtsConnectionHandler;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.constants.Constants;
import com.difisoft.marketcollector.constants.MappingHTS;
import com.difisoft.marketcollector.constants.MarketTypeEnum;
import com.difisoft.marketcollector.model.db.Symbol;
import com.difisoft.model.utils.CompletablePool;
import com.difisoft.model.utils.FutureAllOfUtils;
import com.difisoft.model.utils.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class DownloadInfoService {
    public static final Logger log = LoggerFactory.getLogger(DownloadInfoService.class);
    public static final long TIMEOUT = 30000;

    public AppConf appConf;
    public ObjectMapper objectMapper;
    public CacheService cacheService;

    @Autowired
    public DownloadInfoService(AppConf appConf, ObjectMapper objectMapper,
                               CacheService cacheService) {
        this.appConf = appConf;
        this.objectMapper = objectMapper;
        this.cacheService = cacheService;
    }

    public CompletableFuture<CompletablePool<SymbolInfo>> downloadIndexInfo(BaseHtsConnectionHandler htsConnectionHandler, List<Symbol> symbolList) {
        if (symbolList == null || symbolList.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletablePool<SymbolInfo> pool = new CompletablePool<>(
                100,
                symbolList.stream().map(symbol -> new Pair<>(symbol.getCode(), () -> downloadIndexInfo(symbol, htsConnectionHandler)))
        );
        return pool.executeInPoolChain().thenApply(r -> {
            this.logging(pool, "downloadIndexInfo");
            return pool;
        });
    }

    public CompletableFuture<CompletablePool<SymbolInfo>> downloadStockInfo(BaseHtsConnectionHandler htsConnectionHandler, List<Symbol> stockList) {
        if (stockList == null || stockList.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        CompletablePool<SymbolInfo> pool = new CompletablePool<>(
                100,
                stockList.stream().map(symbol -> new Pair<>(symbol.getCode(), () -> downloadStockInfo(symbol, htsConnectionHandler)))
        );
        return pool.executeInPoolChain().thenApply(r -> {
            this.logging(pool, "downloadStockInfo");
            List<SymbolInfo> success = pool.getFutureResults().getSuccess();
            Calendar currently = Calendar.getInstance();
            if (currently.before(cacheService.getTimeStartReceiveBidAsk()) || currently.after(cacheService.getTimeStopReceiveBidAsk())) {
                log.info("ignore bidOfferList because of before TimeStartReceiveBidAsk");
                success.forEach(stockInfo -> stockInfo.setBidOfferList(null));
            }
            return pool;
        });
    }

    public CompletableFuture<CompletablePool<SymbolInfo>> downloadFuturesInfo(BaseHtsConnectionHandler htsConnectionHandler, List<Symbol> symbolList) {
        if (symbolList == null || symbolList.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletablePool<SymbolInfo> pool = new CompletablePool<>(
                100,
                symbolList.stream().map(symbol -> new Pair<>(symbol.getCode(), () -> downloadFutureInfo(symbol, htsConnectionHandler)))
        );
        return pool.executeInPoolChain().thenApply(r -> {
            this.logging(pool, "downloadFuturesInfo");
            Calendar currently = Calendar.getInstance();
            if (currently.before(cacheService.getTimeStartReceiveBidAsk()) || currently.after(cacheService.getTimeStopReceiveBidAsk())) {
                log.info("ignore bidOfferList because of before TimeStartReceiveBidAsk");
                pool.getFutureResults().getSuccess().forEach(stockInfo -> stockInfo.setBidOfferList(null));
            }
            return pool;
        });
    }

    public CompletableFuture<CompletablePool<SymbolInfo>> downloadCWInfo(BaseHtsConnectionHandler htsConnectionHandler, List<Symbol> cwList) {
        if (cwList == null || cwList.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }


        CompletablePool<SymbolInfo> pool = new CompletablePool<>(
                100,
                cwList.stream().map(symbol -> new Pair<>(symbol.getCode(), () -> downloadCWInfo(symbol, htsConnectionHandler)))
        );
        return pool.executeInPoolChain().thenApply(r -> {
            this.logging(pool, "downloadCWInfo");
            Calendar currently = Calendar.getInstance();
            if (currently.before(cacheService.getTimeStartReceiveBidAsk()) || currently.after(cacheService.getTimeStopReceiveBidAsk())) {
                log.info("ignore bidOfferList because of before TimeStartReceiveBidAsk");
                pool.getFutureResults().getSuccess().forEach(stockInfo -> stockInfo.setBidOfferList(null));
            }
            return pool;
        });
    }


    public <T> void logging(CompletablePool<T> pool, String name) {
        if (pool.getFutureResults().getExceptions().isEmpty() || pool.getMapResult().isEmpty()) {
            log.warn("do {} success: {}, fail: {}", name, pool.getFutureResults().getSuccess().size(),
                    pool.getFutureResults().getExceptions().size());
        } else {
            log.warn("do {} success: {}, fail: {}. fail symbols are: {}", name, pool.getFutureResults().getSuccess().size(),
                    pool.getFutureResults().getExceptions().size(),
                    pool.getMapResult().entrySet().stream().filter(entry -> entry.getValue().getRight() != null)
                            .collect(Collectors.toList()));
        }
    }


    public CompletableFuture<SymbolInfo> downloadStockInfo(Symbol symbol, BaseHtsConnectionHandler htsConnectionHandler) {
        MarketStockCurrentPriceSnd snd = new MarketStockCurrentPriceSnd();
        snd.getStockCode().setValue(symbol.getValidCode());

        MarketStockQuoteListSnd quoteListSnd = new MarketStockQuoteListSnd();
        quoteListSnd.getStockCode().setValue(symbol.getCode());
        quoteListSnd.getIReadCount().setValue(1);

        return new FutureAllOfUtils<SymbolInfo>(null, false)
                .add(
                        htsConnectionHandler.sendMessageFuture(snd, MarketStockCurrentPriceRcv.class, TIMEOUT),
                        (rcv, err, w) -> {
                            log.info("finish download symbolInfo: {} {}", symbol.getCode(), err);
                            if (err != null) {
                                log.error("fail to query info of symbol {}", symbol.getCode(), err);
                                throw new FutureAllOfUtils.StopException(err);
                            }
                            w.setV(MappingHTS.QUERY_STOCK_INFO.apply(rcv));
                            w.getV().setName(symbol.getName());
                            w.getV().setNameEn(symbol.getNameEn());
                            w.getV().setSecuritiesType(symbol.getSecuritiesType());
                            w.getV().setType(symbol.getType(w.getV().getType()));
                            w.getV().setCreatedAt(new Date());
                            w.getV().setExchange(symbol.getExchange());
                            w.getV().setMarketType(symbol.getMarketType());
                        }
                ).add(
                        htsConnectionHandler.sendMessageFuture(quoteListSnd, MarketStockQuoteListRcv.class, TIMEOUT),
                        (rcv, err, w) -> {
                            log.info("finish download stockList: {} {}", symbol.getCode(), err);
                            if (err != null) {
                                log.error("fail to query quote list volume of symbol {}", symbol.getCode(), err);
                                return;
                            }
                            if (!rcv.getItems().isEmpty()) {
                                MarketStockQuoteItem item = rcv.getItems().get(0);
                                w.getV().setMatchingVolume((long) item.getMatchVolume().getValue());
                            } else {
                                w.getV().setMatchingVolume(0L);
                            }
                            w.getV().setName(symbol.getName());
                            w.getV().setNameEn(symbol.getNameEn());
                            w.getV().setSecuritiesType(symbol.getSecuritiesType());
                            w.getV().setType(symbol.getType(w.getV().getType()));
                            w.getV().setCreatedAt(new Date());
                            w.getV().setExchange(symbol.getExchange());
                            w.getV().setMarketType(symbol.getMarketType());
                        }
                ).allOf();
    }


    public CompletableFuture<SymbolInfo> downloadIndexInfo(Symbol symbol, BaseHtsConnectionHandler htsConnectionHandler) {
        MarketIndustryCurrentIndexSnd snd = new MarketIndustryCurrentIndexSnd();
        snd.getIndexCode().setValue(symbol.getValidCode());
        return htsConnectionHandler.sendMessageFuture(snd, MarketIndustryCurrentIndexRcv.class, TIMEOUT).thenApply(rcv -> {
            SymbolInfo indexInfo = MappingHTS.QUERY_INDEX_INFO.apply(rcv);
            indexInfo.setName(symbol.getName());
            indexInfo.setNameEn(symbol.getNameEn());
            indexInfo.setMarketType(symbol.getMarketType());
            indexInfo.setCode(symbol.getCode());
            indexInfo.setRefCode(symbol.getValidCode());
            indexInfo.setCreatedAt(new Date());
            indexInfo.setIsHighlight(appConf.getHighlightMap().getOrDefault(symbol.getCode(), Constants.DEFAULT_HIGHLIGHT_NUMBER));
            return indexInfo;
        });
    }


    public CompletableFuture<SymbolInfo> downloadFutureInfo(Symbol symbol, BaseHtsConnectionHandler htsConnectionHandler) {
        MarketFuturesCurrentPriceSnd snd = new MarketFuturesCurrentPriceSnd();
        snd.getStockCode().setValue(symbol.getValidCode());

        MarketFuturesQuoteListSnd quoteListSnd = new MarketFuturesQuoteListSnd();
        quoteListSnd.getFuturesCode().setValue(symbol.getCode());
        quoteListSnd.getIReadCount().setValue(1);
        return new FutureAllOfUtils<SymbolInfo>(null, false)
                .add(
                        htsConnectionHandler.sendMessageFuture(snd, MarketFuturesCurrentPriceRcv.class, TIMEOUT),
                        (rcv, err, w) -> {
                            if (err != null) {
                                log.error("fail to query info of symbol {}", symbol.getCode());
                                throw new FutureAllOfUtils.StopException(err);
                            }
                            w.setV(MappingHTS.QUERY_FUTURES_INFO.apply(rcv));

                            w.getV().setCode(symbol.getValidCode());
                            w.getV().setName(symbol.getName());
                            w.getV().setNameEn(symbol.getNameEn());
                            w.getV().setSecuritiesType(symbol.getSecuritiesType());
                            w.getV().setExchange(symbol.getExchange());
                            w.getV().setCreatedAt(new Date());
                            if (w.getV().getExchange().equals("15")) {
                                w.getV().setMarketType(MarketTypeEnum.HNX.name());
                            }

                            w.getV().setUnderlyingSymbol(StringUtils.isEmpty(w.getV().getBaseCode()) ? "VN30" : w.getV().getBaseCode());
                            w.getV().setBaseCodeSecuritiesType("VN30".equalsIgnoreCase(w.getV().getUnderlyingSymbol()) ? "INDEX" : "BOND");
                            if (w.getV().getCode().startsWith("VN30")) {
                                // TODO set basis
                            }
                        }
                ).add(
                        htsConnectionHandler.sendMessageFuture(quoteListSnd, MarketFuturesQuoteListRcv.class, TIMEOUT),
                        (data, err, w) -> {
                            if (err != null) {
                                log.error("fail to query quote list volume of symbol {}", symbol.getCode(), err);
                                return;
                            }
                            if (!data.getItems().isEmpty()) {
                                MarketFuturesQuoteItem item = data.getItems().get(0);
                                w.getV().setMatchingVolume((long) item.getMatchVolume().getValue());
                            } else {
                                w.getV().setMatchingVolume(0L);
                            }

                            w.getV().setCode(symbol.getValidCode());
                            w.getV().setName(symbol.getName());
                            w.getV().setNameEn(symbol.getNameEn());
                            w.getV().setSecuritiesType(symbol.getSecuritiesType());
                            w.getV().setExchange(symbol.getExchange());
                            w.getV().setCreatedAt(new Date());
                            if (w.getV().getExchange().equals("15")) {
                                w.getV().setMarketType(MarketTypeEnum.HNX.name());
                            }

                            w.getV().setUnderlyingSymbol(StringUtils.isEmpty(w.getV().getBaseCode()) ? "VN30" : w.getV().getBaseCode());
                            w.getV().setBaseCodeSecuritiesType("VN30".equalsIgnoreCase(w.getV().getUnderlyingSymbol()) ? "INDEX" : "BOND");
                            if (w.getV().getCode().startsWith("VN30")) {
                                // TODO set basis
                            }
                        }
                ).allOf();
    }


    public CompletableFuture<SymbolInfo> downloadCWInfo(Symbol symbol, BaseHtsConnectionHandler htsConnectionHandler) {
        MarketCWCurrentPriceSnd snd = new MarketCWCurrentPriceSnd();
        snd.getCwCode().setValue(symbol.getValidCode());
        MarketCWQuoteListSnd quoteListSnd = new MarketCWQuoteListSnd();
        quoteListSnd.getCwCode().setValue(symbol.getCode());
        quoteListSnd.getIReadCount().setValue(1);


        return new FutureAllOfUtils<SymbolInfo>(null, false)
                .add(
                        htsConnectionHandler.sendMessageFuture(snd, MarketCWCurrentPriceRcv.class, TIMEOUT),
                        (rcv, err, w) -> {
                            if (err != null) {
                                log.error("fail to query info of symbol {}", symbol.getCode());
                                throw new FutureAllOfUtils.StopException(err);
                            }
                            w.setV(MappingHTS.QUERY_CW_INFO.apply(rcv));
                        }
                ).add(
                        htsConnectionHandler.sendMessageFuture(quoteListSnd, MarketCWQuoteListRcv.class, TIMEOUT),
                        (data, err, w) -> {
                            if (err != null) {
                                log.error("fail to query quote list volume of symbol {}", symbol.getCode(), err);
                                return;
                            }

                            if (!data.getItems().isEmpty()) {
                                MarketCWQuoteItem item = data.getItems().get(0);
                                w.getV().setMatchingVolume((long) item.getMatchVolume().getValue());
                            } else {
                                w.getV().setMatchingVolume(0L);
                            }

                            w.getV().setName(symbol.getName());
                            w.getV().setNameEn(symbol.getNameEn());
                            w.getV().setSecuritiesType(symbol.getSecuritiesType());
                            w.getV().setCode(symbol.getValidCode());
                            w.getV().setMarketType(symbol.getMarketType());
                            w.getV().setCreatedAt(new Date());
                            if (StringUtils.isEmpty(w.getV().getUnderlyingSymbol())) {
                                w.getV().setUnderlyingSymbol(w.getV().getCode().substring(1, 4));
                            }
                        }
                ).allOf();
    }
}
