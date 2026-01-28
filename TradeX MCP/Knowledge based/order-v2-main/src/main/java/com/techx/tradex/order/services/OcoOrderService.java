package com.techx.tradex.order.services;

import com.difisoft.kafka.handler.RequestContext;
import com.techx.tradex.order.model.request.*;
import com.techx.tradex.order.model.response.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public interface OcoOrderService {

    CompletableFuture<OcoOrderPlaceResponse> addOcoOrder(OcoOrderPlaceRequest request, RequestContext<OcoOrderPlaceRequest> ctx);

    CompletableFuture<OcoOrderCancelResponse> cancelOcoOrder(OcoOrderCancelRequest request, RequestContext<OcoOrderCancelRequest> ctx);

    CompletableFuture<List<OcoOrderHistoryResponse>> queryOcoOrderHistory(OcoOrderHistoryRequest request, RequestContext<OcoOrderHistoryRequest> ctx);

    CompletableFuture<List<OcoOrderTodayResponse>> queryOcoOrderToday(OcoOrderTodayRequest request, RequestContext<OcoOrderTodayRequest> ctx);

    CompletableFuture<OcoOrderDetailResponse> queryOcoOrderDetail(OcoOrderDetailRequest request, RequestContext<OcoOrderDetailRequest> ctx);
}
