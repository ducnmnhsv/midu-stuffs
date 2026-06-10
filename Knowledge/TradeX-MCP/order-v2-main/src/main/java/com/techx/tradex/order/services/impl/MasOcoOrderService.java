package com.techx.tradex.order.services.impl;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.constants.OcoOrderStatusEnum;
import com.difisoft.model.constants.PlOrderTypeEnum;
import com.difisoft.model.exceptions.GeneralException;
import com.techx.tradex.order.constants.Constants;
import com.techx.tradex.order.dao.BridgeOrderDao;
import com.techx.tradex.order.model.db.OcoOrder;
import com.techx.tradex.order.model.db.ProfitLossOrder;
import com.techx.tradex.order.model.request.*;
import com.techx.tradex.order.model.response.*;
import com.techx.tradex.order.repositories.OcoOrderRepository;
import com.techx.tradex.order.repositories.ProfitLossOrderRepository;
import com.techx.tradex.order.services.CacheService;
import com.techx.tradex.order.services.OcoOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class MasOcoOrderService implements OcoOrderService {
    private static final Logger log = LoggerFactory.getLogger(MasOcoOrderService.class);

    private OcoOrderRepository ocoOrderRepo;
    private ProfitLossOrderRepository profitLossOrderRepo;
    private CacheService cacheService;
    private BridgeOrderDao bridgeOrderDao;

    @Autowired
    public MasOcoOrderService(OcoOrderRepository ocoOrderRepo,
                              ProfitLossOrderRepository profitLossOrderRepo,
                              CacheService cacheService,
                              BridgeOrderDao bridgeOrderDao) {
        this.ocoOrderRepo = ocoOrderRepo;
        this.profitLossOrderRepo = profitLossOrderRepo;
        this.cacheService = cacheService;
        this.bridgeOrderDao = bridgeOrderDao;
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<OcoOrderPlaceResponse> addOcoOrder(OcoOrderPlaceRequest request, RequestContext<OcoOrderPlaceRequest> ctx) {
        log.info("addOcoOrder: {}", request);
        request.validate();
        OcoOrder ocoOrder = request.toOcoOrder();
        SymbolInfo symbolInfo = cacheService.getCacheSymbolInfo().get(request.getCode());
        if (symbolInfo == null || !symbolInfo.getType().equals(SymbolTypeEnum.FUTURES)) {
            throw new GeneralException(Constants.ERROR_INVALID_SYMBOL_CODE);
        }
        ocoOrder.setCurrentPrice(symbolInfo.getLast());
        generateTakeProfitOrder(ocoOrder, symbolInfo);
        return CompletableFuture.completedFuture(OcoOrderPlaceResponse.fromOcoOrder(ocoOrder));
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<OcoOrderCancelResponse> cancelOcoOrder(OcoOrderCancelRequest request, RequestContext<OcoOrderCancelRequest> ctx) {
        log.info("cancelOcoOrder: {}", request);
        request.validate();
        OcoOrder ocoOrder = ocoOrderRepo.findByUsernameAndId(request.getHeaders().getToken().getUserData().getUsername(), request.getOcoOrderId());
        if (ocoOrder == null) {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
        if (!ocoOrder.getStatus().equals(OcoOrderStatusEnum.PENDING)) {
            throw new GeneralException(Constants.OCO_ORDER_INVALID_STATUS);
        }
        ocoOrder.setStatus(OcoOrderStatusEnum.CANCELLED);
        ocoOrder.setCancelledAt(new Date());
        ocoOrder.setCancelledBy("BY_REQUEST");
        ocoOrderRepo.save(ocoOrder);
        cacheService.resetCacheOcoOrder();
        log.info("finish cancelOcoOrder: {}", request);
        return CompletableFuture.completedFuture(new OcoOrderCancelResponse());
    }

    private void generateTakeProfitOrder(OcoOrder ocoOrder, SymbolInfo symbolInfo) {
        ProfitLossOrder plOrder = ProfitLossOrder.fromOcoOrder(ocoOrder, PlOrderTypeEnum.TAKE_PROFIT);
        log.info("takeProfit for ocoOrder: {} with plOrder: {}", ocoOrder, plOrder);
        bridgeOrderDao.placeRealProfitLossOrderSync(plOrder, symbolInfo);
        if (plOrder.getOrderNumber() != null) {
            ocoOrderRepo.save(ocoOrder);
            this.cacheService.addOcoOrderToCache(ocoOrder);
            profitLossOrderRepo.save(plOrder);
            this.cacheService.addPlOrderToCache(plOrder);
        } else {
            log.warn("Error generating takeProfit order from oco. Cancel ocoOrder now.");
            ocoOrder.setFailReason(plOrder.getFailReason());
            ocoOrder.setStatus(OcoOrderStatusEnum.FAILED);
            ocoOrderRepo.save(ocoOrder);
            profitLossOrderRepo.save(plOrder);
        }
    }

    public CompletableFuture<List<OcoOrderHistoryResponse>> queryOcoOrderHistory(OcoOrderHistoryRequest request, RequestContext<OcoOrderHistoryRequest> ctx) {
        log.info("ocoOrder history: {}", request);
        List<OcoOrderHistoryResponse> responses = new ArrayList<>();
        OcoOrderHistoryRequest.TimeAndPage timeAndPage = OcoOrderHistoryRequest.historyAndPageDesc(request, "query ocoOrder history");
        List<OcoOrder> ocoOrderList = ocoOrderRepo.findBy(request.getHeaders().getToken().getUserData().getUsername(), request.getAccountNumber(), request.getCode(),
                request.getSellBuyType(), request.getStatus(), timeAndPage.getFromDate(), timeAndPage.getToDate(), timeAndPage.getPageable()).toList();
        log.info("ocoOrderHisList size: {}", ocoOrderList.size());
        ocoOrderList.forEach(ocoOrder -> responses.add(OcoOrderHistoryResponse.fromOcoOrder(this.cacheService.getObjectMapper(), ocoOrder)));
        log.info("finished queryOcoOrderHistory");
        return CompletableFuture.completedFuture(responses);
    }

    public CompletableFuture<List<OcoOrderTodayResponse>> queryOcoOrderToday(OcoOrderTodayRequest request, RequestContext<OcoOrderTodayRequest> ctx) {
        log.info("ocoOrder today: {}", request);
        List<OcoOrderTodayResponse> responses = new ArrayList<>();
        OcoOrderTodayRequest.TimeAndPage timeAndPage = OcoOrderTodayRequest.todayAndPageDesc(request);
        List<OcoOrder> ocoOrderList = ocoOrderRepo.findBy(request.getUsername(), request.getAccountNumber(), request.getCode(),
                request.getSellBuyType(), request.getStatus(), timeAndPage.getFromDate(), timeAndPage.getToDate(), timeAndPage.getPageable()).toList();
        log.info("ocoOrderToday size: {}", ocoOrderList.size());
        ocoOrderList.forEach(ocoOrder -> responses.add(OcoOrderTodayResponse.fromOcoOrder(cacheService.getObjectMapper(), ocoOrder)));
        log.info("finished ocoOrderToday");
        return CompletableFuture.completedFuture(responses);
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<OcoOrderDetailResponse> queryOcoOrderDetail(OcoOrderDetailRequest request, RequestContext<OcoOrderDetailRequest> ctx) {
        log.info("Query ocoOrder detail {}", request);
        OcoOrder ocoOrder = ocoOrderRepo.findById(request.getId()).orElse(null);
        if (ocoOrder == null) {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
        OcoOrderDetailResponse response = OcoOrderDetailResponse.fromOcoOrder(this.cacheService.getObjectMapper(), ocoOrder);
        return CompletableFuture.completedFuture(response);
    }
}
