package com.techx.tradex.order.services;

import com.difisoft.kafka.handler.RequestContext;
import com.techx.tradex.order.model.request.*;
import com.techx.tradex.order.model.response.BullBearOrderCancelResponse;
import com.techx.tradex.order.model.response.BullBearOrderHistoryResponse;
import com.techx.tradex.order.model.response.BullBearOrderPlaceResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public interface BullBearOrderService {

    CompletableFuture<BullBearOrderPlaceResponse> addBullBearOrder(BullBearOrderPlaceRequest request, RequestContext<BullBearOrderPlaceRequest> ctx);

    CompletableFuture<BullBearOrderCancelResponse> cancelBullBearOrder(BullBearOrderCancelRequest request, RequestContext<BullBearOrderCancelRequest> ctx);

    CompletableFuture<List<BullBearOrderHistoryResponse>> queryBullBearOrderHistory(BullBearOrderHistoryRequest request, RequestContext<BullBearOrderHistoryRequest> ctx);

    CompletableFuture<List<BullBearOrderHistoryResponse>> queryBullBearOrderToday(BullBearOrderTodayRequest request, RequestContext<BullBearOrderTodayRequest> ctx);

    CompletableFuture<BullBearOrderHistoryResponse> queryBullBearOrderDetail(BullBearOrderDetailRequest request, RequestContext<BullBearOrderDetailRequest> ctx);
}
