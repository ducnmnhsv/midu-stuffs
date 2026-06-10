package com.techx.tradex.order.services;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.utils.DefaultUtils;
import com.techx.tradex.order.configurations.AppConf;
import com.techx.tradex.order.constants.Constants;
import com.techx.tradex.order.model.request.*;
import com.techx.tradex.order.model.response.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StopOrderService {
    CompletableFuture<StopOrderPlaceResponse> placeStopOrder(StopOrderPlaceRequest request, RequestContext<StopOrderPlaceRequest> ctx);

    CompletableFuture<StopOrderModifyResponse> modifySpeedStopOrder(StopOrderSpeedModifyRequest request, RequestContext<StopOrderSpeedModifyRequest> ctx);

    CompletableFuture<StopOrderModifyResponse> modifyStopOrder(StopOrderModifyRequest request, RequestContext<StopOrderModifyRequest> ctx);

    CompletableFuture<StopOrderCancelResponse> cancelStopOrder(StopOrderCancelRequest request, RequestContext<StopOrderCancelRequest> ctx);

    CompletableFuture<StopOrderCancelMultiResponse> cancelMultiStopOrders(StopOrderCancelMultiRequest request, RequestContext<StopOrderCancelMultiRequest> ctx);

    CompletableFuture<StopOrderCancelAllResponse> cancelSpeedStopOrder(StopOrderSpeedCancelRequest request, RequestContext<StopOrderSpeedCancelRequest> ctx);

    CompletableFuture<List<StopOrderHistoryResponse>> queryStopOrderHistory(StopOrderHistoryRequest request, RequestContext<StopOrderHistoryRequest> ctx);

    void cancelAllBySchedule();

    CompletableFuture<List<StopOrderHistoryResponse>> queryStopOrderLastUpdate(GetStopOrderLastUpdateRequest request, RequestContext<GetStopOrderLastUpdateRequest> ctx);

    default void validateFromDateToDate(AppConf appConf, ZonedDateTime fromDate, ZonedDateTime toDate, ZonedDateTime currentMarketDate) {
        if (toDate.isBefore(currentMarketDate)) {
            throw new GeneralException(Constants.FROM_DATE_INVALID);
        }

        if (toDate.isBefore(fromDate)) {
            throw new GeneralException(Constants.TO_DATE_MUST_BE_GTE_THAN_FROM_DATE);
        }

        if ((fromDate.isEqual(currentMarketDate) || toDate.isEqual(currentMarketDate))
                && !appConf.isEnablePlaceOrderAfterMarketClose()) {
            if (ZonedDateTime.now(DefaultUtils.VIETNAM_ID).getHour() >= 15) {
                throw new GeneralException(Constants.FROM_DATE_INVALID);
            }
        }
    }

    default void checkStopPriceForOrderValidToday(SellBuyTypeEnum sellBuyType, Double stopPrice, Double marketPrice) {
        if (sellBuyType.equals(SellBuyTypeEnum.SELL)
                && (stopPrice - marketPrice >= 0)) {
            throw new GeneralException(Constants.SELL_STOP_PRICE_CANNOT_GTE_THAN_CURRENT_PRICE, stopPrice.toString(), marketPrice.toString());
        }
        if (sellBuyType.equals(SellBuyTypeEnum.BUY)
                && (stopPrice - marketPrice <= 0)) {
            throw new GeneralException(Constants.BUY_STOP_ORDER_PRICE_CANNOT_LTE_THAN_CURRENT_PRICE, stopPrice.toString(), marketPrice.toString());
        }
    }
}
