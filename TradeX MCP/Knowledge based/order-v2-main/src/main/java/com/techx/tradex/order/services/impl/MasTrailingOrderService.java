package com.techx.tradex.order.services.impl;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.constants.TrailingOrderStatusEnum;
import com.difisoft.model.exceptions.GeneralException;
import com.techx.tradex.order.constants.Constants;
import com.techx.tradex.order.model.db.TrailingOrder;
import com.techx.tradex.order.model.request.TrailingOrderCancelRequest;
import com.techx.tradex.order.model.request.TrailingOrderHistoryRequest;
import com.techx.tradex.order.model.request.TrailingOrderPlaceRequest;
import com.techx.tradex.order.model.response.TrailingOrderAddResponse;
import com.techx.tradex.order.model.response.TrailingOrderCancelResponse;
import com.techx.tradex.order.model.response.TrailingOrderHistoryResponse;
import com.techx.tradex.order.repositories.TrailingOrderRepository;
import com.techx.tradex.order.services.CacheService;
import com.techx.tradex.order.services.TrailingOrderService;
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
public class MasTrailingOrderService implements TrailingOrderService {
    private static final Logger log = LoggerFactory.getLogger(MasTrailingOrderService.class);

    private final TrailingOrderRepository trailingOrderRepo;
    private final CacheService cacheService;

    @Autowired
    public MasTrailingOrderService(TrailingOrderRepository trailingOrderRepo, CacheService cacheService) {
        this.trailingOrderRepo = trailingOrderRepo;
        this.cacheService = cacheService;
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<TrailingOrderAddResponse> addTrailingOrder(TrailingOrderPlaceRequest request, RequestContext<TrailingOrderPlaceRequest> ctx) {
        request.validate();
        TrailingOrder trailingOrder = request.toTrailingOrder();
        SymbolInfo symbolInfo = cacheService.getCacheSymbolInfo().get(request.getCode());
        if (symbolInfo == null || !symbolInfo.getType().equals(SymbolTypeEnum.FUTURES)) {
            throw new GeneralException(Constants.ERROR_INVALID_SYMBOL_CODE);
        }
        trailingOrder.setCurrentPrice(symbolInfo.getLast());
        if (trailingOrder.getSellBuyType().equals(SellBuyTypeEnum.SELL)) {
            trailingOrder.setStopPrice(trailingOrder.getCurrentPrice() + trailingOrder.getTrailingAmount());  // f(0) = P(0) + D
        } else {
            trailingOrder.setStopPrice(trailingOrder.getCurrentPrice() - trailingOrder.getTrailingAmount());  // f(0) = P(0) - D
        }
        trailingOrderRepo.save(trailingOrder);
        cacheService.addTrailingOrderToCache(trailingOrder);
        return CompletableFuture.completedFuture(TrailingOrderAddResponse.fromTrailingOrder(trailingOrder));
    }


    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<List<TrailingOrderHistoryResponse>> queryTrailingOrderHistory(TrailingOrderHistoryRequest request, RequestContext<TrailingOrderHistoryRequest> ctx) {
        log.info("start queryTrailingOrderHistory");
        List<TrailingOrderHistoryResponse> responses = new ArrayList<>();
        String username = request.getUsername();
        SellBuyTypeEnum sellBuyType = request.getSellBuyType();
        TrailingOrderStatusEnum status = null;
        if (request.getStatus() != null) {
            status = TrailingOrderStatusEnum.valueOf(request.getStatus());
        }
        TrailingOrderHistoryRequest.TimeAndPage timeAndPage = TrailingOrderHistoryRequest.todayAndPageDesc(request);

        List<TrailingOrder> trailingOrderList = trailingOrderRepo.findBy(username, request.getAccountNumber(), request.getCode(),
                sellBuyType, status, timeAndPage.getFromDate(), timeAndPage.getToDate(), request.getLastTrailingOrderId(), timeAndPage.getPageable()).toList();
        log.info("trailingOrderList size: {}", trailingOrderList.size());
        trailingOrderList.forEach(trailingOrder -> responses.add(TrailingOrderHistoryResponse.fromTrailingOrder(trailingOrder)));
        log.info("finished queryTrailingOrderHistory");
        return CompletableFuture.completedFuture(responses);
    }


    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<TrailingOrderCancelResponse> cancelTrailingOrder(TrailingOrderCancelRequest request, RequestContext<TrailingOrderCancelRequest> ctx) {
        synchronized (cacheService.getCacheTrailingOrder()) {
            request.validate();
            TrailingOrder trailingOrder = cacheService.getCacheTrailingOrder().get(request.getTrailingOrderId());
            if (trailingOrder == null) {
                trailingOrder = trailingOrderRepo.findByUsernameAndId(request.getHeaders().getToken().getUserData().getUsername(), request.getTrailingOrderId());
                if (trailingOrder == null) {
                    throw new GeneralException(Constants.ERROR_TRAILING_ORDER_NOT_FOUND);
                } else {
                    throw new GeneralException(Constants.ERROR_TRAILING_ORDER_INVALID_STATUS);
                }
            } else if (!trailingOrder.getUsername().equalsIgnoreCase(request.getUsername())) {
                throw new GeneralException(Constants.ERROR_TRAILING_ORDER_NOT_FOUND);
            }
            trailingOrder.setStatus(TrailingOrderStatusEnum.CANCELLED);
            trailingOrder.setCancelledAt(new Date());
            trailingOrder.setCancelledBy("BY_REQUEST");
            trailingOrderRepo.save(trailingOrder);
            cacheService.getCacheTrailingOrder().remove(request.getTrailingOrderId());
            return CompletableFuture.completedFuture(new TrailingOrderCancelResponse());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void cancelAllBySchedule() {
        log.info("start cancelAllBySchedule");
        synchronized (cacheService.getCacheTrailingOrder()) {
            List<TrailingOrder> trailingOrderList = new ArrayList<>(cacheService.getCacheTrailingOrder().values());
            trailingOrderList.forEach(trailingOrder -> {
                trailingOrder.setCancelledAt(new Date());
                trailingOrder.setStatus(TrailingOrderStatusEnum.CANCELLED);
                trailingOrder.setCancelledBy("BY_SCHEDULE");
            });
            trailingOrderRepo.saveAll(trailingOrderList);
            cacheService.getCacheTrailingOrder().clear();
        }
        log.info("finish cancelAllBySchedule");
    }

}
