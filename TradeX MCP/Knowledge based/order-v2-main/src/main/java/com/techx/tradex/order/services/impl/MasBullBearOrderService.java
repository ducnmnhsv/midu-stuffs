package com.techx.tradex.order.services.impl;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.constants.BullBearOrderStatusEnum;
import com.difisoft.model.constants.ProfitLossOrderStatusEnum;
import com.difisoft.model.exceptions.GeneralException;
import com.techx.tradex.order.constants.Constants;
import com.techx.tradex.order.dao.BridgeOrderDao;
import com.techx.tradex.order.model.db.BullBearOrder;
import com.techx.tradex.order.model.db.ProfitLossOrder;
import com.techx.tradex.order.model.request.*;
import com.techx.tradex.order.model.response.BullBearOrderCancelResponse;
import com.techx.tradex.order.model.response.BullBearOrderHistoryResponse;
import com.techx.tradex.order.model.response.BullBearOrderPlaceResponse;
import com.techx.tradex.order.model.response.MasBosOrderCancelResponse;
import com.techx.tradex.order.repositories.BullBearOrderRepository;
import com.techx.tradex.order.repositories.ProfitLossOrderRepository;
import com.techx.tradex.order.services.BullBearOrderService;
import com.techx.tradex.order.services.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class MasBullBearOrderService implements BullBearOrderService {
    private static final Logger log = LoggerFactory.getLogger(MasBullBearOrderService.class);

    private BullBearOrderRepository bullBearOrderRepo;
    private ProfitLossOrderRepository profitLossOrderRepo;
    private CacheService cacheService;
    private BridgeOrderDao bridgeOrderDao;

    @Autowired
    public MasBullBearOrderService(BullBearOrderRepository bullBearOrderRepo,
                                   ProfitLossOrderRepository profitLossOrderRepo,
                                   CacheService cacheService, BridgeOrderDao bridgeOrderDao) {
        this.bullBearOrderRepo = bullBearOrderRepo;
        this.profitLossOrderRepo = profitLossOrderRepo;
        this.cacheService = cacheService;
        this.bridgeOrderDao = bridgeOrderDao;
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<BullBearOrderPlaceResponse> addBullBearOrder(BullBearOrderPlaceRequest request) {
        log.info("addBullBearOrder: {}", request);
        request.validate();
        BullBearOrder bullBearOrder = request.toBullBearOrder();
        SymbolInfo symbolInfo = cacheService.getCacheSymbolInfo().get(request.getCode());
        if (symbolInfo == null || !symbolInfo.getType().equals(SymbolTypeEnum.FUTURES)) {
            throw new GeneralException(Constants.ERROR_INVALID_SYMBOL_CODE);
        }
        generateOpenPositionOrder(bullBearOrder, symbolInfo);
        return CompletableFuture.completedFuture(BullBearOrderPlaceResponse.fromBullBearOrder(bullBearOrder));
    }

    private void generateOpenPositionOrder(BullBearOrder bullBearOrder, SymbolInfo symbolInfo) {
        ProfitLossOrder plOrder = ProfitLossOrder.fromBullBearOrder(bullBearOrder);
        log.info("create openPosition order: {}", plOrder);
        bridgeOrderDao.placeRealProfitLossOrderSync(plOrder, symbolInfo);
        if (plOrder.getOrderNumber() != null) {
            bullBearOrderRepo.save(bullBearOrder);
            profitLossOrderRepo.save(plOrder);
            this.cacheService.addPlOrderToCache(plOrder);
            this.cacheService.addBullBearOrderToCache(bullBearOrder);
        } else {
            log.warn("Error generating openPosition order from bullbear. Cancel bullbear now.");
            bullBearOrder.setFailReason(plOrder.getFailReason());
            bullBearOrder.setStatus(BullBearOrderStatusEnum.FAILED);
            bullBearOrderRepo.save(bullBearOrder);
            profitLossOrderRepo.save(plOrder);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<BullBearOrderCancelResponse> cancelBullBearOrder(BullBearOrderCancelRequest request) throws IOException, ExecutionException, InterruptedException {
        log.info("cancelBullBearOrder: {}", request);
        request.validate();
        BullBearOrder bullBearOrder = bullBearOrderRepo.findByUsernameAndId(request.getHeaders().getToken().getUserData().getUsername(), request.getBullBearOrderId());
        if (bullBearOrder == null) {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
        if (!bullBearOrder.getStatus().equals(BullBearOrderStatusEnum.PENDING)) {
            throw new GeneralException(Constants.BULLBEAR_ORDER_INVALID_STATUS);
        }
        bullBearOrder.setStatus(BullBearOrderStatusEnum.CANCELLED);
        bullBearOrder.setCancelledAt(new Date());
        bullBearOrder.setCancelledBy("BY_REQUEST");
        bullBearOrderRepo.save(bullBearOrder);
        log.info("cancel all profitLossOrder:");
        // cancel all profitLossOrder
        List<ProfitLossOrder> profitLossOrderList = profitLossOrderRepo.findByBullBearOrder(bullBearOrder);
        if (!profitLossOrderList.isEmpty()) {
            for (ProfitLossOrder profitLossOrder : profitLossOrderList) {
                log.info("cancel profitLossOrder: {}", profitLossOrder);
                if (!profitLossOrder.getStatus().equals(ProfitLossOrderStatusEnum.PENDING)) {
                    throw new GeneralException(Constants.PL_ORDER_INVALID_STATUS);
                }
                List<MasBosOrderCancelResponse> response = bridgeOrderDao.cancelRealOrderSync(profitLossOrder);
                profitLossOrder.setStatus(ProfitLossOrderStatusEnum.CANCELLED);
                profitLossOrder.setCancelledAt(new Date());
                profitLossOrder.setCancelledBy("BY_BULL_BEAR_ORDER");
            }
        }
        cacheService.resetCacheBullBearOrder();
        log.info("finish cancelBullBearOrder: {}", request);
        return CompletableFuture.completedFuture(new BullBearOrderCancelResponse());
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<List<BullBearOrderHistoryResponse>> queryBullBearOrderHistory(BullBearOrderHistoryRequest request) {
        log.info("query bullbear history request: {}", request);
        BullBearOrderHistoryRequest.TimeAndPage timeAndPage = BullBearOrderHistoryRequest.historyAndPageDesc(request, "query bullBear history");
        List<BullBearOrder> bullBearOrderList = bullBearOrderRepo.findBy(request.getUsername(), request.getAccountNumber(), request.getCode(),
                request.getSellBuyType(), request.getStatus(), timeAndPage.getFromDate(), timeAndPage.getToDate(), timeAndPage.getPageable()).toList();
        List<BullBearOrderHistoryResponse> response = new ArrayList<>();
        log.info("bullBearOrderHistory size: {}", bullBearOrderList.size());
        bullBearOrderList.forEach(bullBearOrder -> response.add(BullBearOrderHistoryResponse.fromBullBearOrder(this.cacheService.getObjectMapper(), bullBearOrder)));
        log.info("finished query bullBearOrder history");
        return CompletableFuture.completedFuture(response);
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<List<BullBearOrderHistoryResponse>> queryBullBearOrderToday(BullBearOrderTodayRequest request) {
        log.info("query bullbear today request: {}", request);
        BullBearOrderHistoryRequest.TimeAndPage timeAndPage = BullBearOrderHistoryRequest.todayAndPageDesc(request);
        List<BullBearOrder> bullBearOrderList = bullBearOrderRepo.findBy(request.getUsername(), request.getAccountNumber(), request.getCode(),
                request.getSellBuyType(), request.getStatus(), timeAndPage.getFromDate(), timeAndPage.getToDate(), timeAndPage.getPageable()).toList();
        List<BullBearOrderHistoryResponse> response = new ArrayList<>();
        log.info("bullBearOrderToday size: {}", bullBearOrderList.size());
        bullBearOrderList.forEach(bullBearOrder -> response.add(BullBearOrderHistoryResponse.fromBullBearOrder(this.cacheService.getObjectMapper(), bullBearOrder)));
        log.info("finished query bullBearOrder today");
        return CompletableFuture.completedFuture(response);
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<BullBearOrderHistoryResponse> queryBullBearOrderDetail(BullBearOrderDetailRequest request) {
        log.info("query bullbear order detail request: {}", request);
        BullBearOrder bullBearOrder = bullBearOrderRepo.findById(request.getId()).orElse(null);
        if (bullBearOrder == null) {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
        BullBearOrderHistoryResponse response = BullBearOrderHistoryResponse.fromBullBearOrder(this.cacheService.getObjectMapper(), bullBearOrder);
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public CompletableFuture<BullBearOrderPlaceResponse> addBullBearOrder(BullBearOrderPlaceRequest request, RequestContext<BullBearOrderPlaceRequest> ctx) {
        return null;
    }

    @Override
    public CompletableFuture<BullBearOrderCancelResponse> cancelBullBearOrder(BullBearOrderCancelRequest request, RequestContext<BullBearOrderCancelRequest> ctx) {
        return null;
    }

    @Override
    public CompletableFuture<List<BullBearOrderHistoryResponse>> queryBullBearOrderHistory(BullBearOrderHistoryRequest request, RequestContext<BullBearOrderHistoryRequest> ctx) {
        return null;
    }

    @Override
    public CompletableFuture<List<BullBearOrderHistoryResponse>> queryBullBearOrderToday(BullBearOrderTodayRequest request, RequestContext<BullBearOrderTodayRequest> ctx) {
        return null;
    }

    @Override
    public CompletableFuture<BullBearOrderHistoryResponse> queryBullBearOrderDetail(BullBearOrderDetailRequest request, RequestContext<BullBearOrderDetailRequest> ctx) {
        return null;
    }
}
