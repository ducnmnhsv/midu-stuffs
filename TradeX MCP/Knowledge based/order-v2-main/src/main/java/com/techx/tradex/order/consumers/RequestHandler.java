package com.techx.tradex.order.consumers;


import com.difisoft.job.JobHandler;
import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.kafka.model.ForwardException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.configurations.AppConf;
import com.techx.tradex.order.model.request.*;
import com.techx.tradex.order.services.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

@Service
@Slf4j
public class RequestHandler extends JobHandler {
    private final AppConf appConf;
    private final String theOtherClusterId;

    @Autowired
    public RequestHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            StopOrderService stopOrderService,
            TrailingOrderService trailingOrderService,
            OcoOrderService ocoOrderService,
            BullBearOrderService bullBearOrderService,
            CacheService cacheService
    ) {
        super(objectMapper, appConf.getKafkaBootstraps(),
                appConf.getMode() != AppConf.Mode.ENGINE ? appConf.getClusterId() : String.format("%s-engine", appConf.getClusterId()),
                appConf.getNodeId(),
                appConf.getMaxThread()
        );
        this.appConf = appConf;
        String cId = appConf.getClusterId();
        String oId = String.format("%s-engine", appConf.getClusterId());
        if (appConf.getMode() == AppConf.Mode.ENGINE) {
            this.theOtherClusterId = cId;
        } else {
            this.theOtherClusterId = oId;
        }
        UriBuilder uriBuilder = this.uriBuilder();
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "job:/api/v1/order/resetCache", Object.class, cacheService::resetHandler);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "post:/api/v1/trailingOrder", TrailingOrderPlaceRequest.class, trailingOrderService::addTrailingOrder);
        addUriWhenModeNotEquals(AppConf.Mode.ENGINE, uriBuilder, "get:/api/v1/trailingOrder/history", TrailingOrderHistoryRequest.class, trailingOrderService::queryTrailingOrderHistory);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "put:/api/v1/trailingOrder/cancel", TrailingOrderCancelRequest.class, trailingOrderService::cancelTrailingOrder);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "post:/api/v1/stopOrder", StopOrderPlaceRequest.class, stopOrderService::placeStopOrder);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "put:/api/v1/stopOrder/modify", StopOrderModifyRequest.class, stopOrderService::modifyStopOrder);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "put:/api/v1/stopOrder/cancel", StopOrderCancelRequest.class, stopOrderService::cancelStopOrder);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "put:/api/v1/stopOrder/cancel/multi", StopOrderCancelMultiRequest.class, stopOrderService::cancelMultiStopOrders);
        addUriWhenModeNotEquals(AppConf.Mode.ENGINE, uriBuilder, "get:/api/v1/stopOrder/history", StopOrderHistoryRequest.class, stopOrderService::queryStopOrderHistory);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "put:/api/v1/stopOrder/speedModify", StopOrderSpeedModifyRequest.class, stopOrderService::modifySpeedStopOrder);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "put:/api/v1/stopOrder/speedCancel", StopOrderSpeedCancelRequest.class, stopOrderService::cancelSpeedStopOrder);
        addUriWhenModeNotEquals(AppConf.Mode.ENGINE, uriBuilder, "get:/api/v1/stopOrder/lastUpdate", GetStopOrderLastUpdateRequest.class, stopOrderService::queryStopOrderLastUpdate);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "post:/api/v1/ocoOrder", OcoOrderPlaceRequest.class, ocoOrderService::addOcoOrder);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "put:/api/v1/ocoOrder/cancel", OcoOrderCancelRequest.class, ocoOrderService::cancelOcoOrder);
        addUriWhenModeNotEquals(AppConf.Mode.ENGINE, uriBuilder, "get:/api/v1/ocoOrder/history", OcoOrderHistoryRequest.class, ocoOrderService::queryOcoOrderHistory);
        addUriWhenModeNotEquals(AppConf.Mode.ENGINE, uriBuilder, "get:/api/v1/ocoOrder/today", OcoOrderTodayRequest.class, ocoOrderService::queryOcoOrderToday);
        addUriWhenModeNotEquals(AppConf.Mode.ENGINE, uriBuilder, "get:/api/v1/ocoOrder/detail", OcoOrderDetailRequest.class, ocoOrderService::queryOcoOrderDetail);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "post:/api/v1/bullBear", BullBearOrderPlaceRequest.class, bullBearOrderService::addBullBearOrder);
        addUriWhenModeNotEquals(AppConf.Mode.QUERY, uriBuilder, "put:/api/v1/bullBear/cancel", BullBearOrderCancelRequest.class, bullBearOrderService::cancelBullBearOrder);
        addUriWhenModeNotEquals(AppConf.Mode.ENGINE, uriBuilder, "get:/api/v1/bullBear/history", BullBearOrderHistoryRequest.class, bullBearOrderService::queryBullBearOrderHistory);
        addUriWhenModeNotEquals(AppConf.Mode.ENGINE, uriBuilder, "get:/api/v1/bullBear/today", BullBearOrderTodayRequest.class, bullBearOrderService::queryBullBearOrderToday);
        addUriWhenModeNotEquals(AppConf.Mode.ENGINE, uriBuilder, "get:/api/v1/bullBear/detail", BullBearOrderDetailRequest.class, bullBearOrderService::queryBullBearOrderDetail);
        uriBuilder.end();
    }

    private <T> void addUriWhenModeNotEquals(AppConf.Mode notEqualMode, UriBuilder uriBuilder, String uri, Class<T> clazz, BiFunction<T, RequestContext<T>, Object> controller) {
        if (notEqualMode == null || appConf.getMode() != notEqualMode) {
            uriBuilder.add(uri, clazz, controller);
        } else {
            uriBuilder.add(uri, clazz, (t, ctx) -> {
                this.producer.send(new ProducerRecord<>(theOtherClusterId, ctx.getOrigin().getRawRecord().key(), ctx.getOrigin().getRawRecord().value()));
                throw new ForwardException();
            });
        }
    }
}
