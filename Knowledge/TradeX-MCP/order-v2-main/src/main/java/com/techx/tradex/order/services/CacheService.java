package com.techx.tradex.order.services;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.utils.ConvertUtils;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.market.model.v2.db.SymbolQuote;
import com.difisoft.model.constants.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.configurations.AppConf;
import com.techx.tradex.order.constants.Constants;
import com.techx.tradex.order.dao.BridgeOrderDao;
import com.techx.tradex.order.model.OrderMatchNotify;
import com.techx.tradex.order.model.db.BullBearOrder;
import com.techx.tradex.order.model.db.OcoOrder;
import com.techx.tradex.order.model.db.ProfitLossOrder;
import com.techx.tradex.order.model.db.TrailingOrder;
import com.techx.tradex.order.model.response.MasBosOrderCancelResponse;
import com.techx.tradex.order.repositories.*;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Data
public class CacheService {
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private AppConf appConf;
    private RequestSender requestSender;
    private ObjectMapper objectMapper;
    private TrailingOrderRepository trailingOrderRepo;
    private StopOrderRepository stopOrderRepo;
    private ProfitLossOrderRepository profitLossOrderRepo;
    private OcoOrderRepository ocoOrderRepo;
    private BullBearOrderRepository bullBearOrderRepo;
    private BridgeOrderDao bridgeOrderDao;
    private MarketRedisDao redisDao;

    @Autowired
    public CacheService(AppConf appConf,
                        RequestSender requestSender,
                        ObjectMapper objectMapper,
                        TrailingOrderRepository trailingOrderRepo,
                        StopOrderRepository stopOrderRepo,
                        ProfitLossOrderRepository profitLossOrderRepo,
                        OcoOrderRepository ocoOrderRepo,
                        BullBearOrderRepository bullBearOrderRepo,
                        BridgeOrderDao bridgeOrderDao,
                        MarketRedisDao redisDao) {
        this.appConf = appConf;
        this.requestSender = requestSender;
        this.objectMapper = objectMapper;
        this.trailingOrderRepo = trailingOrderRepo;
        this.stopOrderRepo = stopOrderRepo;
        this.profitLossOrderRepo = profitLossOrderRepo;
        this.ocoOrderRepo = ocoOrderRepo;
        this.bullBearOrderRepo = bullBearOrderRepo;
        this.bridgeOrderDao = bridgeOrderDao;
        this.redisDao = redisDao;
    }

    private final Map<String, SymbolInfo> cacheSymbolInfo = new ConcurrentHashMap<>();
    private final Map<Long, TrailingOrder> cacheTrailingOrder = new ConcurrentHashMap<>();
    private final Map<Long, ProfitLossOrder> cacheProfitLossOrder = new ConcurrentHashMap<>();
    private final Map<Long, OcoOrder> cacheOcoOrder = new ConcurrentHashMap<>();
    private final Map<Long, BullBearOrder> cacheBullBearOrder = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        this.reset();
    }

    public CompletableFuture<Void> resetHandler(Object request, RequestContext<Object> ctx) {
        return this.reset();
    }

    public CompletableFuture<Void> reset() {
        log.info("start resetCache");
        long t1 = System.currentTimeMillis();

        resetCacheSymbolInfo();
        resetCacheTrailingOrder();
        resetCacheOcoOrder();
        resetCacheBullBearOrder();
        resetCacheProfitLossOrder();

        long t2 = System.currentTimeMillis();
        log.info("finish reset cacheService take: {}", (t2 - t1));
        return CompletableFuture.completedFuture(null);
    }

    public void resetCacheTrailingOrder() {
        log.info("start resetCacheTrailingOrder");
        long t1 = System.currentTimeMillis();
        List<TrailingOrder> trailingOrderList = trailingOrderRepo.findTodayPendingTrailingOrder();
        this.cacheTrailingOrder.clear();
        trailingOrderList.forEach(trailingOrder -> this.cacheTrailingOrder.put(trailingOrder.getId(), trailingOrder));
        log.info("trailingOrder pending: {}", trailingOrderList.size());
        long t2 = System.currentTimeMillis();
        log.info("finish resetCacheTrailingOrder take: {}", (t2 - t1));
    }

    public void resetCacheProfitLossOrder() {
        synchronized (cacheProfitLossOrder) {
            log.info("start resetCacheProfitLossOrder");
            long t1 = System.currentTimeMillis();
            List<ProfitLossOrder> profitLossOrderList = profitLossOrderRepo.findByStatus(ProfitLossOrderStatusEnum.PENDING);
            this.cacheProfitLossOrder.clear();
            profitLossOrderList.forEach(profitLossOrder -> this.cacheProfitLossOrder.put(profitLossOrder.getId(), profitLossOrder));
            log.info("profitLossOrder pending: {}", profitLossOrderList.size());
            long t2 = System.currentTimeMillis();
            log.info("finish resetCacheProfitLossOrder take: {}", (t2 - t1));
        }
    }

    public void resetCacheOcoOrder() {
        log.info("start resetCacheOcoOrder");
        long t1 = System.currentTimeMillis();
        List<OcoOrder> ocoOrderList = ocoOrderRepo.findByUnmatchQuantityGreaterThanAndStatusIn(0, Arrays.asList(OcoOrderStatusEnum.PENDING, OcoOrderStatusEnum.COMPLETED));
        this.cacheOcoOrder.clear();
        ocoOrderList.forEach(ocoOrder -> this.cacheOcoOrder.put(ocoOrder.getId(), ocoOrder));
        log.info("ocoOrder pending: {}", ocoOrderList.size());
        long t2 = System.currentTimeMillis();
        log.info("finish resetCacheOcoOrder take: {}", (t2 - t1));
    }

    public void resetCacheBullBearOrder() {
        log.info("start cacheBullBearOrder");
        long t1 = System.currentTimeMillis();
        List<BullBearOrder> bullBearOrderList = bullBearOrderRepo.findByStatus(BullBearOrderStatusEnum.PENDING);
        this.cacheOcoOrder.clear();
        bullBearOrderList.forEach(bullBearOrder -> this.cacheBullBearOrder.put(bullBearOrder.getId(), bullBearOrder));
        log.info("bullBearOrder pending: {}", bullBearOrderList.size());
        long t2 = System.currentTimeMillis();
        log.info("finish cacheBullBearOrder take: {}", (t2 - t1));
    }

    public void addTrailingOrderToCache(TrailingOrder trailingOrder) {
        cacheTrailingOrder.put(trailingOrder.getId(), trailingOrder);
    }

    public void addOcoOrderToCache(OcoOrder ocoOrder) {
        cacheOcoOrder.put(ocoOrder.getId(), ocoOrder);
    }

    public void addBullBearOrderToCache(BullBearOrder bullBearOrder) {
        cacheBullBearOrder.put(bullBearOrder.getId(), bullBearOrder);
    }

    public void addPlOrderToCache(ProfitLossOrder plOrder) {
        cacheProfitLossOrder.put(plOrder.getId(), plOrder);
    }

    public void resetCacheSymbolInfo() {
        log.info("start resetCacheSymbolInfo");
        long t1 = System.currentTimeMillis();
        List<com.difisoft.market.model.v2.db.SymbolInfo> symbolInfoList = redisDao.getAllSymbolInfo();
        log.info("finish load symbolInfo from redis");
        this.cacheSymbolInfo.clear();

        for (com.difisoft.market.model.v2.db.SymbolInfo symbolInfo : symbolInfoList) {
            this.cacheSymbolInfo.put(symbolInfo.getCode(), symbolInfo);
        }
        log.info("finish reload SymbolInfo");
        log.info("total symbolInfo size: " + this.cacheSymbolInfo.size());
        long t2 = System.currentTimeMillis();
        log.info("finish resetCacheSymbolInfo take: {}", (t2 - t1));
    }

    public void updateCacheByQuote(SymbolQuote symbolQuote) {
        log.info("updateCacheByQuote");
        String code = symbolQuote.getCode();
        this.cacheSymbolInfo.computeIfPresent(code, (k, info) -> {
            ConvertUtils.updateByQuote(info, symbolQuote);
            return info;
        });
    }

    public void checkAndExecuteTrailingOrder(SymbolQuote symbolQuote) {
        log.info("checkAndExecuteTrailingOrder with quote: {}", symbolQuote);
        SymbolInfo symbolInfo = cacheSymbolInfo.get(symbolQuote.getCode());
        cacheTrailingOrder.forEach((trailingOrderId, trailingOrder) -> {
            if (trailingOrder.getCode().equals(symbolQuote.getCode())) {
                if (trailingOrder.getSellBuyType().equals(SellBuyTypeEnum.BUY)) {
                    if (symbolQuote.getLast() - trailingOrder.getStopPrice() >= 0) {
                        cacheTrailingOrder.remove(trailingOrderId);
                        log.info("activeTrailingOrder -> remove trailingOrder from cache: {}", trailingOrder);
                        bridgeOrderDao.placeRealOrder(trailingOrder, symbolInfo);
                    } else {
                        trailingOrder.setStopPrice(Math.min(trailingOrder.getStopPrice(), (symbolQuote.getLast() + trailingOrder.getTrailingAmount())));
                        trailingOrder.setCurrentPrice(symbolQuote.getLast());
                    }
                } else {
                    if (symbolQuote.getLast() - trailingOrder.getStopPrice() <= 0) {
                        cacheTrailingOrder.remove(trailingOrderId);
                        log.info("activeTrailingOrder -> remove trailingOrder from cache: {}", trailingOrder);
                        bridgeOrderDao.placeRealOrder(trailingOrder, symbolInfo);
                    } else {
                        trailingOrder.setStopPrice(Math.max(trailingOrder.getStopPrice(), (symbolQuote.getLast() - trailingOrder.getTrailingAmount())));
                        trailingOrder.setCurrentPrice(symbolQuote.getLast());
                    }
                }
            }
        });
    }

    public void updateTrailingOrderToDatabase() {
        synchronized (cacheTrailingOrder) {
            log.info("updateTrailingOrderToDatabase, size: {}", cacheTrailingOrder.values().size());
            trailingOrderRepo.saveAll(cacheTrailingOrder.values());
        }
    }

    public void updateConditionalOrder(OrderMatchNotify orderMatchNotify) {
        cacheProfitLossOrder.forEach((plOrderId, plOrder) -> {
            if (plOrder.getOrderNumber().equals(orderMatchNotify.getOrderNumber())
                    && plOrder.getOrderGroupNumber().equals(orderMatchNotify.getOrderGroupNumber())) {
                plOrder.updateByOrderMatch(orderMatchNotify);
                ProfitLossOrder plOrderInRepo = profitLossOrderRepo.findById(plOrderId).get();
                plOrderInRepo.updateByOrderMatch(orderMatchNotify);
                if (plOrder.getBullBearOrder() != null) { // this order is generated by bullbear order
                    Long bullBearId = plOrder.getBullBearOrder().getId();
                    BullBearOrder bullBearOrderInRepo = bullBearOrderRepo.findById(bullBearId).get();
                    bullBearOrderInRepo.updateByOrderMatch(orderMatchNotify);
                    if (plOrder.getQuantity() - plOrder.getMatchQuantity() == 0) { // order is fullfill, remove plOrder + bullbearOrder from cache
                        plOrderInRepo.setStatus(ProfitLossOrderStatusEnum.COMPLETED);
                        generateOcoOrderAndSaveBullBear(bullBearOrderInRepo, orderMatchNotify);
                        cacheProfitLossOrder.remove(plOrderId);
                        cacheBullBearOrder.remove(bullBearId);
                    } else {
                        bullBearOrderRepo.save(bullBearOrderInRepo);
                    }
                } else if (plOrder.getOcoOrder() != null) { // this order is generate by oco order
                    Long ocoOrderId = plOrder.getOcoOrder().getId();
                    OcoOrder ocoOrderInRepo = ocoOrderRepo.findById(ocoOrderId).get();
                    ocoOrderInRepo.updateByOrderMatch(orderMatchNotify);
                    if (plOrder.getQuantity() - plOrder.getMatchQuantity() == 0) { // order is fulfilled
                        plOrderInRepo.setStatus(ProfitLossOrderStatusEnum.COMPLETED);
                        ocoOrderInRepo.setStatus(OcoOrderStatusEnum.COMPLETED);
                        cacheProfitLossOrder.remove(plOrderId);
                        cacheOcoOrder.remove(ocoOrderId);
                    } else {
                        cacheOcoOrder.get(ocoOrderId).updateByOrderMatch(orderMatchNotify);
                    }
                    ocoOrderRepo.save(ocoOrderInRepo);
                }
                profitLossOrderRepo.save(plOrderInRepo);
            }
        });
    }

    public void checkAndExecuteOcoOrder(SymbolQuote symbolQuote) {
        SymbolInfo symbolInfo = cacheSymbolInfo.get(symbolQuote.getCode());
        this.cacheOcoOrder.forEach((ocoOrderId, ocoOrder) -> {
            if (ocoOrder.getCode().equals(symbolQuote.getCode())) {
                if ((ocoOrder.getSellBuyType().equals(SellBuyTypeEnum.SELL) && symbolQuote.getLast() - ocoOrder.getTriggerLossPrice() <= 0) ||
                        (ocoOrder.getSellBuyType().equals(SellBuyTypeEnum.BUY) && symbolQuote.getLast() - ocoOrder.getTriggerLossPrice() >= 0)) {
                    Boolean cancelTakeProfitOrderSuccess = false;
                    try {
                        cancelTakeProfitOrderSuccess = ocoCancelTakeProfitOrder(ocoOrder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (cancelTakeProfitOrderSuccess.equals(true)) {
                        ocoGenerateCutLossOrder(ocoOrder, symbolInfo);
                        this.cacheOcoOrder.remove(ocoOrderId);
                    }
                }
            }
        });
    }

    public Boolean ocoCancelTakeProfitOrder(OcoOrder ocoOrder) {
        ProfitLossOrder takeProfitOrderToCancel = cacheProfitLossOrder.values().stream()
                .filter(plOrder -> plOrder.getOcoOrder().getId().equals(ocoOrder.getId())
                        && plOrder.getProfitLossType().equals(PlOrderTypeEnum.TAKE_PROFIT))
                .collect(Collectors.toList())
                .get(0);
        try {
            List<MasBosOrderCancelResponse> cancelResponse = bridgeOrderDao.cancelRealOrderSync(takeProfitOrderToCancel);
            if (cancelResponse.get(0).getSuccess().equals(true)) {
                cacheProfitLossOrder.remove(takeProfitOrderToCancel.getId());
                ProfitLossOrder takeProfitOrder = profitLossOrderRepo.findById(takeProfitOrderToCancel.getId()).get();
                takeProfitOrder.setStatus(ProfitLossOrderStatusEnum.CANCELLED);
                takeProfitOrder.setFailReason(Constants.CUT_LOSS_ORDER_IS_GENERATED);
                profitLossOrderRepo.save(takeProfitOrder);
                log.info("cancelled takeProfit order of this ocoOrder successfully, id {}", takeProfitOrderToCancel.getId());
                return true;
            } else {
                log.warn("Error cancel takeProfit order . Keep takeProfitOrder + ocoOrder and wait until it matches next time.");
            }
        } catch (Exception e) {
            log.warn("Error cancel takeProfit order . Keep takeProfitOrder + ocoOrder and wait until it matches next time." + e);
        }
        return false;
    }

    public Boolean ocoGenerateCutLossOrder(OcoOrder ocoOrder, SymbolInfo symbolInfo) {
        ProfitLossOrder plOrder = ProfitLossOrder.fromOcoOrder(ocoOrder, PlOrderTypeEnum.CUT_LOSS);
        log.info("cutLoss for ocoOrder: {} with plOrder: {}", ocoOrder, plOrder);
        bridgeOrderDao.placeRealProfitLossOrderSync(plOrder, symbolInfo);
        profitLossOrderRepo.save(plOrder);
        OcoOrder ocoOrderInRepo = ocoOrderRepo.findById(ocoOrder.getId()).get();
        if (plOrder.getOrderNumber() != null) {
            ocoOrderInRepo.setStatus(OcoOrderStatusEnum.COMPLETED);
            ocoOrderRepo.save(ocoOrderInRepo);
            this.addPlOrderToCache(plOrder);
            return true;
        } else {
            ocoOrderInRepo.setStatus(OcoOrderStatusEnum.FAILED);
            ocoOrderInRepo.setFailReason(plOrder.getFailReason());
            ocoOrderRepo.save(ocoOrderInRepo);
            return false;
        }
    }

    public ProfitLossOrder ocoGenerateTakeProfitOrder(OcoOrder ocoOrder) {
        SymbolInfo symbolInfo = cacheSymbolInfo.get(ocoOrder.getCode());
        ProfitLossOrder plOrder = ProfitLossOrder.fromOcoOrder(ocoOrder, PlOrderTypeEnum.TAKE_PROFIT);
        log.info("takeProfit for ocoOrder: {} with plOrder: {}", ocoOrder, plOrder);
        bridgeOrderDao.placeRealProfitLossOrderSync(plOrder, symbolInfo);
        if (plOrder.getOrderNumber() != null) {
            ocoOrder.setStatus(OcoOrderStatusEnum.PENDING);
        } else {
            log.warn("Error generating takeProfit order from oco. Cancel ocoOrder now.");
            ocoOrder.setFailReason(plOrder.getFailReason());
            ocoOrder.setStatus(OcoOrderStatusEnum.FAILED);
        }
        return plOrder;
    }

    public void generateOcoOrderAndSaveBullBear(BullBearOrder bullBearOrderInRepo, OrderMatchNotify orderMatchNotify) {
        OcoOrder ocoOrder = OcoOrder.fromBullBearOrder(bullBearOrderInRepo);
        ocoOrder.setCurrentPrice(orderMatchNotify.getOrderPrice());
        ProfitLossOrder profitLossOrder = ocoGenerateTakeProfitOrder(ocoOrder);
        if (profitLossOrder.getStatus().equals(ProfitLossOrderStatusEnum.PENDING)) {
            bullBearOrderInRepo.setStatus(BullBearOrderStatusEnum.COMPLETED);
            bullBearOrderRepo.save(bullBearOrderInRepo);
            ocoOrderRepo.save(ocoOrder);
            profitLossOrderRepo.save(profitLossOrder);
            this.addOcoOrderToCache(ocoOrder);
            this.addPlOrderToCache(profitLossOrder);
        } else {
            bullBearOrderInRepo.setStatus(BullBearOrderStatusEnum.FAILED);
            bullBearOrderInRepo.setFailReason(ocoOrder.getFailReason());
            bullBearOrderRepo.save(bullBearOrderInRepo);
            ocoOrderRepo.save(ocoOrder);
            profitLossOrderRepo.save(profitLossOrder);
        }
    }

}
