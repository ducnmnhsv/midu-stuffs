package com.techx.tradex.order.services;

import com.difisoft.kafka.handler.RequestContext;
import com.techx.tradex.order.model.request.TrailingOrderCancelRequest;
import com.techx.tradex.order.model.request.TrailingOrderHistoryRequest;
import com.techx.tradex.order.model.request.TrailingOrderPlaceRequest;
import com.techx.tradex.order.model.response.TrailingOrderAddResponse;
import com.techx.tradex.order.model.response.TrailingOrderCancelResponse;
import com.techx.tradex.order.model.response.TrailingOrderHistoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public interface TrailingOrderService {

    CompletableFuture<TrailingOrderAddResponse> addTrailingOrder(TrailingOrderPlaceRequest request, RequestContext<TrailingOrderPlaceRequest> ctx);

    CompletableFuture<List<TrailingOrderHistoryResponse>> queryTrailingOrderHistory(TrailingOrderHistoryRequest request, RequestContext<TrailingOrderHistoryRequest> ctx);

    CompletableFuture<TrailingOrderCancelResponse> cancelTrailingOrder(TrailingOrderCancelRequest request, RequestContext<TrailingOrderCancelRequest> ctx);

    void cancelAllBySchedule();

}
