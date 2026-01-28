package com.techx.tradex.order.dao;

import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.constants.ErrorCodeEnums;
import com.difisoft.model.constants.ProfitLossOrderStatusEnum;
import com.difisoft.model.constants.StopOrderStatusEnum;
import com.difisoft.model.constants.TrailingOrderStatusEnum;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.kafka.Message;
import com.difisoft.model.responses.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.configurations.AppConf;
import com.techx.tradex.order.constants.Constants;
import com.techx.tradex.order.model.db.ProfitLossOrder;
import com.techx.tradex.order.model.db.StopOrder;
import com.techx.tradex.order.model.db.TrailingOrder;
import com.techx.tradex.order.model.realtime.UpdatedOrder;
import com.techx.tradex.order.model.request.MasBosOrderCancelRequest;
import com.techx.tradex.order.model.request.TtlBosOrderPlaceRequest;
import com.techx.tradex.order.model.response.MasBosOrderCancelResponse;
import com.techx.tradex.order.model.response.MasBosOrderPlaceResponse;
import com.techx.tradex.order.repositories.ProfitLossOrderRepository;
import com.techx.tradex.order.repositories.StopOrderRepository;
import com.techx.tradex.order.repositories.TrailingOrderRepository;
import com.techx.tradex.order.services.RequestSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TtlBridgeOrderDao implements BridgeOrderDao {
    private static final Logger log = LoggerFactory.getLogger(TtlBridgeOrderDao.class);

    private final AppConf appConf;
    private final RequestSender requestSender;
    private final ObjectMapper objectMapper;
    private final StopOrderRepository stopOrderRepo;
    private final TrailingOrderRepository trailingOrderRepo;
    private final ProfitLossOrderRepository plOrderRepo;

    @Autowired
    public TtlBridgeOrderDao(AppConf appConf, RequestSender requestSender,
                             ObjectMapper objectMapper,
                             TrailingOrderRepository trailingOrderRepo,
                             StopOrderRepository stopOrderRepository,
                             ProfitLossOrderRepository plOrderRepo) {
        this.appConf = appConf;
        this.requestSender = requestSender;
        this.objectMapper = objectMapper;
        this.stopOrderRepo = stopOrderRepository;
        this.trailingOrderRepo = trailingOrderRepo;
        this.plOrderRepo = plOrderRepo;
    }

    @Async
    @Override
    public void placeRealOrder(StopOrder stopOrder, SymbolInfo symbolInfo) {
        log.info("placeRealOrder by stopOrder: {} _ symbolInfo: {}", stopOrder, symbolInfo);
        try {
            stopOrder.setOrderedAt(ZonedDateTime.now());
            TtlBosOrderPlaceRequest request = TtlBosOrderPlaceRequest.fromStopOrder(stopOrder, symbolInfo);
            log.info("Request PlaceRealOrder from stopOrder {}", request);
            Message<?> message;
            if (SymbolTypeEnum.FUTURES.equals(symbolInfo.getType())) { //place derivatives order
                message = requestSender.sendAsyncRequest(appConf.getTopics().getMasRestBridge(), appConf.getUri().getMasDrPlaceOrder(), appConf.getClusterId(), request).get();
            } else { //place eqt order
                message = requestSender.sendAsyncRequest(appConf.getTopics().getMasRestBridge(), appConf.getUri().getMasEqtPlaceOrder(), appConf.getClusterId(), request).get();
            }
            log.info("placeRealOrder response from bos: {}", message);
            try {
                MasBosOrderPlaceResponse response = message.getResponse(objectMapper, new TypeReference<>() {
                });
                log.info("mas-rest-bridge order result: {}", response);
                stopOrder.setStatus(StopOrderStatusEnum.COMPLETED);
                stopOrder.setOrderNumber(response.getOrderNumber());
            } catch (GeneralException e) {
                stopOrder.setStatus(StopOrderStatusEnum.FAILED);
                stopOrder.setFailReason(objectMapper.writeValueAsString(e.getResponseStatus()));
            }
        } catch (Exception ex) {
            log.error("error while place Order {}: _stopOrder: {}", stopOrder, ex);
            stopOrder.setStatus(StopOrderStatusEnum.FAILED);
            Status status = new Status();
            status.setCode(ErrorCodeEnums.INTERNAL_SERVER_ERROR.name());
            try {
                stopOrder.setFailReason(objectMapper.writeValueAsString(status));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        stopOrderRepo.save(stopOrder);
        UpdatedOrder updatedOrder = UpdatedOrder.fromStopOrder(stopOrder);
        requestSender.sendRequestNoResponseSafe(appConf.getTopics().getUpdateConditionalOrder(), "Update", updatedOrder);
    }

    @Override
    public ProfitLossOrder placeRealProfitLossOrderSync(ProfitLossOrder plOrder, SymbolInfo symbolInfo) {
        log.info("placeRealOrder by plOrder: {} _ symbolInfo: {}", plOrder, symbolInfo);

        try {
            plOrder.setOrderedAt(new Date());
            TtlBosOrderPlaceRequest request = TtlBosOrderPlaceRequest.fromProfitLossOrder(plOrder, symbolInfo);
            // send request to mas-rest-bridge
            Message<?> message = requestSender.sendAsyncRequest(appConf.getTopics().getMasRestBridge(), appConf.getUri().getMasDrPlaceOrder(), appConf.getClusterId(), request).get();
            log.info("placeRealOrder response from bos: {}", message);
            try {
                MasBosOrderPlaceResponse response = message.getResponse(objectMapper, new TypeReference<>() {
                });
                log.info("mas-rest-bridge order result: {}", response);
                plOrder.setStatus(ProfitLossOrderStatusEnum.PENDING);
                plOrder.setOrderNumber(response.getOrderNumber());
                plOrder.setOrderGroupNumber(response.getOrderGroupID());
            } catch (GeneralException e) {
                plOrder.setStatus(ProfitLossOrderStatusEnum.FAILED);
                plOrder.setFailReason(objectMapper.writeValueAsString(e.getResponseStatus()));
            }
        } catch (Exception ex) {
            log.error("error while place Order {}: plOrder: {}", plOrder, ex);
            plOrder.setStatus(ProfitLossOrderStatusEnum.FAILED);
            Status status = new Status();
            status.setCode(ErrorCodeEnums.INTERNAL_SERVER_ERROR.name());
            try {
                plOrder.setFailReason(objectMapper.writeValueAsString(status));
            } catch (JsonProcessingException e) {
                log.error("fail to set fail reason for status {}", status, e);
            }
        }
        return plOrder;
    }

    @Async
    @Override
    public void placeRealOrder(ProfitLossOrder plOrder, SymbolInfo symbolInfo) {
        placeRealProfitLossOrderSync(plOrder, symbolInfo);
        plOrderRepo.save(plOrder);
    }

    @Override
    public MasBosOrderPlaceResponse placeRealOrderSync(ProfitLossOrder order, SymbolInfo symbolInfo) throws IOException, ExecutionException, InterruptedException {
        log.info("placeRealOrder by plOrder: {} _ symbolInfo: {}", order, symbolInfo);
        TtlBosOrderPlaceRequest request = TtlBosOrderPlaceRequest.fromProfitLossOrder(order, symbolInfo);
        // send request to mas-rest-bridge
        Message<?> message = requestSender.sendAsyncRequest(appConf.getTopics().getMasRestBridge(), appConf.getUri().getMasDrPlaceOrder(), appConf.getClusterId(), request).get();
        log.info("placeRealOrder response from bos: {}", message);

        return message.getResponse(objectMapper, new TypeReference<>() {
        });
    }

    @Async
    @Override
    public void placeRealOrder(TrailingOrder trailingOrder, SymbolInfo symbolInfo) {
        log.info("placeRealOrder by trailingOrder: {} _ symbolInfo: {}", trailingOrder, symbolInfo);
        try {
            trailingOrder.setOrderedAt(new Date());
            TtlBosOrderPlaceRequest request = TtlBosOrderPlaceRequest.fromTrailingOrder(trailingOrder, symbolInfo);
            // send request to mas-rest-bridge
            Message<?> message = requestSender.sendAsyncRequest(appConf.getTopics().getMasRestBridge(), appConf.getUri().getMasDrPlaceOrder(), appConf.getClusterId(), request).get();
            log.info("placeRealOrder response from bos: {}", message);
            try {
                MasBosOrderPlaceResponse response = message.getResponse(objectMapper, new TypeReference<>() {
                });
                trailingOrder.setStatus(TrailingOrderStatusEnum.COMPLETED);
                trailingOrder.setOrderNumber(response.getOrderNumber());
            } catch (GeneralException e) {
                trailingOrder.setStatus(TrailingOrderStatusEnum.FAILED);
                trailingOrder.setErrorCode(e.getResponseStatus().getCode());
                trailingOrder.setFailReason(e.getResponseStatus().getCode());
            }

        } catch (Exception ex) {
            log.error("error while placeRealOrder {}: _trailingOrder: {}", trailingOrder, ex);
            trailingOrder.setStatus(TrailingOrderStatusEnum.FAILED);
            if (ex instanceof GeneralException) {
                trailingOrder.setErrorCode(((GeneralException) ex).getCode());
            } else {
                trailingOrder.setErrorCode(Constants.ERROR_INTERNAL_SERVER_ERROR);
            }
            trailingOrder.setFailReason(ex.toString());
        }
        trailingOrderRepo.save(trailingOrder);
    }

    @Override
    public List<MasBosOrderCancelResponse> cancelRealOrderSync(ProfitLossOrder profitLossOrder) throws ExecutionException, InterruptedException {
        log.info("cancelRealOrder by profitLossOrder: : {}", profitLossOrder);
        MasBosOrderCancelRequest request = MasBosOrderCancelRequest.fromProfitLossOrder(profitLossOrder);
        // send request to mas-rest-bridge
        Message<?> message = requestSender.sendAsyncRequest(appConf.getTopics().getMasRestBridge(), appConf.getUri().getMasDrCancelOrder(), appConf.getClusterId(), request).get();
        log.info("cancelRealOrder response from bos: {}", message);
        return message.getResponse(objectMapper, new TypeReference<>() {
        });
    }

}
