package com.techx.tradex.order.services.impl;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.constants.ErrorCodeEnums;
import com.difisoft.model.constants.StopOrderStatusEnum;
import com.difisoft.model.constants.StopOrderTypeEnum;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.exceptions.InvalidFormatException;
import com.difisoft.model.exceptions.SubErrorsException;
import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.model.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.configurations.AppConf;
import com.techx.tradex.order.constants.Constants;
import com.techx.tradex.order.model.db.StopOrder;
import com.techx.tradex.order.model.request.GetStopOrderLastUpdateRequest;
import com.techx.tradex.order.model.request.StopOrderCancelMultiRequest;
import com.techx.tradex.order.model.request.StopOrderCancelRequest;
import com.techx.tradex.order.model.request.StopOrderHistoryRequest;
import com.techx.tradex.order.model.request.StopOrderModifyRequest;
import com.techx.tradex.order.model.request.StopOrderPlaceRequest;
import com.techx.tradex.order.model.request.StopOrderSpeedCancelRequest;
import com.techx.tradex.order.model.request.StopOrderSpeedModifyRequest;
import com.techx.tradex.order.model.response.StopOrderCancelAllResponse;
import com.techx.tradex.order.model.response.StopOrderCancelMultiResponse;
import com.techx.tradex.order.model.response.StopOrderCancelResponse;
import com.techx.tradex.order.model.response.StopOrderHistoryResponse;
import com.techx.tradex.order.model.response.StopOrderModifyResponse;
import com.techx.tradex.order.model.response.StopOrderPlaceResponse;
import com.techx.tradex.order.repositories.StopOrderRepository;
import com.techx.tradex.order.services.CacheService;
import com.techx.tradex.order.services.OrderTriggerService;
import com.techx.tradex.order.services.StopOrderService;
import com.techx.tradex.order.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class StopOrderServiceImpl implements StopOrderService {
    private static final Logger log = LoggerFactory.getLogger(StopOrderServiceImpl.class);

    private final AppConf appConf;
    private final StopOrderRepository stopOrderRepo;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;
    private final OrderTriggerService orderTriggerService;

    public StopOrderServiceImpl(
            AppConf appConf,
            StopOrderRepository stopOrderRepo,
            ObjectMapper objectMapper,
            CacheService cacheService,
            OrderTriggerService orderTriggerService
    ) {
        this.appConf = appConf;
        this.stopOrderRepo = stopOrderRepo;
        this.objectMapper = objectMapper;
        this.cacheService = cacheService;
        this.orderTriggerService = orderTriggerService;
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<StopOrderPlaceResponse> placeStopOrder(StopOrderPlaceRequest request, RequestContext<StopOrderPlaceRequest> ctx) {
        request.validate();
        log.info("Place stop order request {} with platformInHeaders {}",
                request, request.getHeaders());
        if (!appConf.isAccountCaseSensitive()) {
            request.getHeaders().getToken().getUserData().setUsername(request.getHeaders().getToken().getUserData().getUsername().toLowerCase());
            request.setAccountNumber(request.getAccountNumber().toUpperCase());
            if (request.getSubNumber() != null) {
                request.setSubNumber(request.getSubNumber().toUpperCase());
            }
        }
        SymbolInfo symbolInfo = cacheService.getCacheSymbolInfo().get(request.getCode());
        log.info("symbolInfo: {}", symbolInfo);
        if (symbolInfo == null) {
            throw new GeneralException(Constants.ERROR_INVALID_SYMBOL_CODE);
        }

        if (!StringUtils.isNotBlank(request.getDeviceUniqueId())) {
            throw new SubErrorsException(ErrorCodeEnums.INVALID_PARAMETER.name())
                    .add(Constants.FIELD_IS_REQUIRED, "deviceUniqueId", Collections.singletonList("deviceUniqueId"));
        }

        ZonedDateTime currentMarketDate = Utils.getCurrentMarketDate();
        ZonedDateTime fromDate = null;
        try {
            fromDate = parseAtAndDefault(request.getFromDate(), currentMarketDate);
        } catch (DateTimeParseException e) {
            throw new InvalidFormatException("fromDate");
        }
        ZonedDateTime toDate = null;
        try {
            toDate = parseAtAndDefault(request.getToDate(), currentMarketDate);
        } catch (DateTimeParseException e) {
            throw new InvalidFormatException("toDate");
        }
        validateFromDateToDate(appConf, fromDate, toDate, currentMarketDate);

        if (request.getStopPrice() != null && request.getStopPrice() > 0 && fromDate.isEqual(currentMarketDate)) {
            checkStopPriceForOrderValidToday(request.getSellBuyType(), request.getStopPrice(), symbolInfo.getLast());
        }
        if (request.getOrderType().equals(StopOrderTypeEnum.STOP.name()) && request.getOrderPrice() != null && request.getOrderPrice() > 0) {
            throw new GeneralException("CANNOT SET ORDER PRICE FOR STOP ORDER");
        }
        List<StopOrder> existedOrders = stopOrderRepo.findExistedStopOrder(request.getCode(), request.getStopPrice(),
                null, request.getSellBuyType(), request.getOrderPrice(),
                request.getAccountNumber(), request.getSubNumber(), fromDate, toDate);
        if (!existedOrders.isEmpty()) {
            throw new GeneralException(Constants.ALREADY_EXISTED_ERROR);
        }

        StopOrder stopOrder = request.toStopOrder(fromDate, toDate);
        stopOrder.setSecuritiesType(symbolInfo.getSecuritiesType());
        stopOrder.setRemark(request.getRemark());
        stopOrder = stopOrderRepo.save(stopOrder);
        stopOrder.setRemark(stopOrder.getRemark() + "^soid:" + stopOrder.getId());
        stopOrderRepo.saveAndFlush(stopOrder);
        log.info("order id: {}, same current day: {}", stopOrder.getId(), fromDate.isEqual(currentMarketDate));
        if (fromDate.isEqual(currentMarketDate)) {
            orderTriggerService.addOrder(stopOrder);
        }
        return CompletableFuture.completedFuture(StopOrderPlaceResponse.fromStopOrder(stopOrder));
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<StopOrderModifyResponse> modifySpeedStopOrder(StopOrderSpeedModifyRequest request, RequestContext<StopOrderSpeedModifyRequest> ctx) {
        request.validate();
        SymbolInfo symbolInfo = cacheService.getCacheSymbolInfo().get(request.getCode());
        if (symbolInfo == null) {
            throw new GeneralException(Constants.ERROR_INVALID_SYMBOL_CODE);
        }
        if (!appConf.isAccountCaseSensitive()) {
            request.getHeaders().getToken().getUserData().setUsername(request.getHeaders().getToken().getUserData().getUsername().toLowerCase());
            request.setAccountNumber(request.getAccountNumber().toUpperCase());
            if (request.getSubNumber() != null) {
                request.setSubNumber(request.getSubNumber().toUpperCase());
            }
        }
        log.info("symbolInfo: {}", symbolInfo);
        ZonedDateTime currentMarketDate = Utils.getCurrentMarketDate();
        StopOrder dbStopOrder = stopOrderRepo.findSpeedStopOrder(request.getCode(), request.getStopPrice(), request.getSellBuyType(),
                request.getAccountNumber(), request.getSubNumber(), currentMarketDate).orElse(null);
        if (dbStopOrder == null) {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
        checkStopPriceForOrderValidToday(request.getSellBuyType(), request.getNewStopPrice(), symbolInfo.getLast());
        Long orderQuantity = dbStopOrder.getQuantity();
        // tim stopOrder co thong tin chung voi các thong tin duoc sua (newStopPrice)
        //find stopOrder with same information with the one being modified, except for quantity, then accumulate their quantities and remove one order
        StopOrder similarStopOrder = stopOrderRepo.findSpeedStopOrder(request.getCode(), request.getNewStopPrice(), request.getSellBuyType(),
                request.getAccountNumber(), request.getSubNumber(), currentMarketDate).orElse(null);
        if (similarStopOrder != null) {
            similarStopOrder.setStatus(StopOrderStatusEnum.CANCELLED);
            similarStopOrder.setCancelledAt(ZonedDateTime.now());
            similarStopOrder.setCancelledBy("BY_SPEED_MODIFY");
            orderQuantity += similarStopOrder.getQuantity();
            stopOrderRepo.save(similarStopOrder);
            orderTriggerService.removeOrder(similarStopOrder);
        }
        dbStopOrder.setStopPrice(request.getNewStopPrice());
        dbStopOrder.setQuantity(orderQuantity);
        stopOrderRepo.save(dbStopOrder);
        orderTriggerService.addOrder(dbStopOrder);
        return CompletableFuture.completedFuture(new StopOrderModifyResponse());
    }


    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<StopOrderModifyResponse> modifyStopOrder(StopOrderModifyRequest request, RequestContext<StopOrderModifyRequest> ctx) {
        request.validate();
        Long stopOrderId = request.getStopOrderId();

        if (!appConf.isAccountCaseSensitive()) {
            request.getHeaders().getToken().getUserData().setUsername(request.getHeaders().getToken().getUserData().getUsername().toLowerCase());
        }

        StopOrder dbStopOrder = stopOrderRepo.findByUsernameAndId(request.getUsername(), stopOrderId);
        if (dbStopOrder == null) {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
        if (dbStopOrder.getStatus() != StopOrderStatusEnum.PENDING) {
            throw new GeneralException(Constants.STOP_ORDER_INVALID_STATUS);
        }

        SymbolInfo symbolInfo = cacheService.getCacheSymbolInfo().get(dbStopOrder.getCode());
        if (symbolInfo == null) {
            throw new GeneralException(Constants.ERROR_INVALID_SYMBOL_CODE);
        }
        log.info("symbolInfo: {}", symbolInfo);

        ZonedDateTime currentMarketDate = Utils.getCurrentMarketDate();
        ZonedDateTime fromDate = null;
        try {
            fromDate = parseAtAndDefault(request.getFromDate(), currentMarketDate);
        } catch (DateTimeParseException e) {
            throw new InvalidFormatException("fromDate");
        }
        ZonedDateTime toDate = null;
        try {
            toDate = parseAtAndDefault(request.getToDate(), currentMarketDate);
            ;
        } catch (DateTimeParseException e) {
            throw new InvalidFormatException("toDate");
        }
        validateFromDateToDate(appConf, fromDate, toDate, currentMarketDate);

        //if newFromDate is today, validate price
        if (request.getStopPrice() > 0 && fromDate.isEqual(currentMarketDate)) {
            checkStopPriceForOrderValidToday(dbStopOrder.getSellBuyType(), request.getStopPrice(), symbolInfo.getLast());
        }
        List<StopOrder> similarStopOrders = stopOrderRepo.findSameStopOrder(stopOrderId, dbStopOrder.getCode(), request.getStopPrice(),
                dbStopOrder.getSellBuyType(), dbStopOrder.getOrderType(), request.getOrderPrice(), dbStopOrder.getAccountNumber(), fromDate, toDate);
        if (similarStopOrders.size() > 0) {
            throw new GeneralException(Constants.ALREADY_EXISTED_ERROR);
        }

        dbStopOrder.setStopPrice(request.getStopPrice());
        dbStopOrder.setQuantity(request.getOrderQuantity());
        if (request.getOrderPrice() != null && request.getOrderPrice() > 0) {
            dbStopOrder.setOrderPrice(request.getOrderPrice());
        }
        dbStopOrder.setFromDate(fromDate);
        dbStopOrder.setToDate(toDate);
        stopOrderRepo.save(dbStopOrder);
        if (fromDate.isEqual(currentMarketDate)) {
            orderTriggerService.addOrder(dbStopOrder);
        } else {
            orderTriggerService.removeOrder(dbStopOrder);
        }
        return CompletableFuture.completedFuture(new StopOrderModifyResponse());
    }


    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<StopOrderCancelResponse> cancelStopOrder(StopOrderCancelRequest request, RequestContext<StopOrderCancelRequest> ctx) {
        request.validate();
        Long stopOrderId = request.getStopOrderId();

        if (!appConf.isAccountCaseSensitive()) {
            request.getHeaders().getToken().getUserData().setUsername(request.getHeaders().getToken().getUserData().getUsername().toLowerCase());
        }
        StopOrder stopOrder = stopOrderRepo.findByUsernameAndId(request.getUsername(), stopOrderId);
        if (stopOrder == null) {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
        if (!stopOrder.getStatus().equals(StopOrderStatusEnum.PENDING)) {
            throw new GeneralException(Constants.STOP_ORDER_INVALID_STATUS);
        }
        stopOrder.setStatus(StopOrderStatusEnum.CANCELLED);
        stopOrder.setCancelledAt(ZonedDateTime.now());
        stopOrder.setCancelledBy("BY_REQUEST");

        stopOrderRepo.save(stopOrder);
        orderTriggerService.removeOrder(stopOrder);
        return CompletableFuture.completedFuture(new StopOrderCancelResponse());
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<StopOrderCancelAllResponse> cancelSpeedStopOrder(StopOrderSpeedCancelRequest request, RequestContext<StopOrderSpeedCancelRequest> ctx) {
        request.validate();

        if (!appConf.isAccountCaseSensitive()) {
            request.getHeaders().getToken().getUserData().setUsername(request.getHeaders().getToken().getUserData().getUsername().toLowerCase());
            request.setAccountNumber(request.getAccountNumber().toUpperCase());
            if (request.getSubNumber() != null) {
                request.setSubNumber(request.getSubNumber().toUpperCase());
            }
        }

        List<StopOrder> stopOrderList = stopOrderRepo.findStopOrderToSpeedCancel(request.getUsername(), request.getAccountNumber(), request.getSubNumber(), request.getSellBuyType(), request.getCode(), request.getStopPrice(), StopOrderStatusEnum.PENDING);
        if (stopOrderList.size() > 0) {
            stopOrderList.forEach(stopOrder -> {
                stopOrder.setStatus(StopOrderStatusEnum.CANCELLED);
                stopOrder.setCancelledAt(ZonedDateTime.now());
                stopOrder.setCancelledBy("BY_SPEED_CANCEL");
            });
            stopOrderRepo.saveAll(stopOrderList);
            stopOrderList.forEach(orderTriggerService::removeOrder);
        } else {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
        return CompletableFuture.completedFuture(new StopOrderCancelAllResponse());
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<List<StopOrderHistoryResponse>> queryStopOrderHistory(StopOrderHistoryRequest request, RequestContext<StopOrderHistoryRequest> ctx) {
        String prefixLog = String.format("queryStopOrderHistory_%s", request.getAccountNumber());
        log.info("{} -- request: {}", prefixLog, Utils.objectToStringJsonIgnoreError(request));
        ZonedDateTime currentMarketDate = Utils.getCurrentMarketDate();
        ZonedDateTime fromDate = null;
        try {
            fromDate = parse(StringUtils.isEmpty(request.getFromDate()) ? Constants.DEFAULT_STOP_ORDER_HISTORY_FROM_DATE : request.getFromDate());
        } catch (DateTimeParseException e) {
            throw new InvalidFormatException("fromDate");
        }
        ZonedDateTime toDate = null;
        try {
            toDate = parseAtAndDefault(request.getToDate(), currentMarketDate);
        } catch (DateTimeParseException e) {
            throw new InvalidFormatException("toDate");
        }
        String userType = request.getType();

        if (!appConf.isAccountCaseSensitive()) {
            request.getHeaders().getToken().getUserData().setUsername(request.getHeaders().getToken().getUserData().getUsername().toLowerCase());
            request.setAccountNumber(request.getAccountNumber().toUpperCase());
            if (request.getSubNumber() != null) {
                request.setSubNumber(request.getSubNumber().toUpperCase());
            }
        }

        StopOrderStatusEnum status = StringUtils.isEmpty(request.getStatus()) ? null : StopOrderStatusEnum.valueOf(request.getStatus());
        StopOrderTypeEnum orderType = StringUtils.isEmpty(request.getOrderType()) ? null : StopOrderTypeEnum.valueOf(request.getOrderType());
        int fetchCount = request.getFetchCount() == null ? Constants.DEFAULT_FETCH_COUNT : request.getFetchCount();
        Sort sortByIdDesc = Sort.by(Sort.Direction.DESC, "id");
        String userName = "BROKER".equals(userType) ? null : request.getUsername();
        Pageable pageable = PageRequest.of(0, fetchCount, sortByIdDesc);
        log.info("{} -- request db: accountNumber: {}, userName: {}, code: {}, sellBuyType: {}, orderType: {}, status: {}, fromDate: {}, toDate: {}, pageable: {}"
                , prefixLog, request.getAccountNumber(), userName
                , request.getCode(), request.getSellBuyType(),
                orderType, status, fromDate, toDate, Utils.objectToStringJsonIgnoreError(pageable));
        List<StopOrder> stopOrderList = stopOrderRepo.findHistoryBy(request.getAccountNumber(), userName
                , request.getCode(), request.getSellBuyType(),
                orderType, status, fromDate, toDate, request.getLastStopOrderId(), pageable).toList();
        log.info("{} -- stopOrderList size: {}, id: {}", prefixLog, stopOrderList.size(), stopOrderList.stream().map(StopOrder::getId).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(stopOrderList)) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        List<StopOrderHistoryResponse> responses = new ArrayList<>();
        stopOrderList.forEach(stopOrder -> responses.add(StopOrderHistoryResponse.fromStopOrder(stopOrder, objectMapper)));
        log.info("{} -- responses size: {}", prefixLog, responses.size());
        return CompletableFuture.completedFuture(responses);
    }

    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<StopOrderCancelMultiResponse> cancelMultiStopOrders(StopOrderCancelMultiRequest request, RequestContext<StopOrderCancelMultiRequest> ctx) {
        request.validate();

        if (!appConf.isAccountCaseSensitive()) {
            request.getHeaders().getToken().getUserData().setUsername(request.getHeaders().getToken().getUserData().getUsername().toLowerCase());
        }
        List<StopOrder> stopOrders = stopOrderRepo.findAllById(request.getIdList());
        request.getIdList().removeAll(stopOrders.stream().map(StopOrder::getId).collect(Collectors.toList()));
        if (request.getIdList().size() > 0) {
            log.error("These orders don't exist: {}", request.getIdList());
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
        stopOrders.forEach(stopOrder -> {
            if (stopOrder.getStatus() != StopOrderStatusEnum.PENDING) {
                log.error("{} has invalid status", stopOrder.getId());
                throw new GeneralException(Constants.STOP_ORDER_INVALID_STATUS);
            }
            if (!stopOrder.getUsername().equals(request.getUsername())) {
                log.error("{} doesn't belong to {}", stopOrder.getId(), stopOrder.getAccountNumber());
                throw new GeneralException(Constants.OBJECT_NOT_FOUND);
            }
            stopOrder.setStatus(StopOrderStatusEnum.CANCELLED);
            stopOrder.setCancelledAt(ZonedDateTime.now());
            stopOrder.setCancelledBy("BY_REQUEST");
        });
        stopOrderRepo.saveAll(stopOrders);
        stopOrders.forEach(orderTriggerService::removeOrder);
        return CompletableFuture.completedFuture(new StopOrderCancelMultiResponse());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelAllBySchedule() {
        log.info("start cancelAllBySchedule");
        List<StopOrder> stopOrderList = stopOrderRepo.findExpiredStopOrder();
        if (stopOrderList.size() > 0) {
            stopOrderList.forEach(stopOrder -> {
                stopOrder.setStatus(StopOrderStatusEnum.CANCELLED);
                stopOrder.setCancelledAt(ZonedDateTime.now());
                stopOrder.setCancelledBy("BY_SCHEDULE");
            });
            stopOrderRepo.saveAll(stopOrderList);
            orderTriggerService.cleanAll();
        }
        log.info("finish cancelAllBySchedule");
    }

    @Override
    public CompletableFuture<List<StopOrderHistoryResponse>> queryStopOrderLastUpdate(GetStopOrderLastUpdateRequest request, RequestContext<GetStopOrderLastUpdateRequest> ctx) {
        log.info("{} queryStopOrderLastUpdate: {}", ctx.getId(), request);
        List<StopOrder> stopOrderList = new ArrayList<>();
        if (request.getFromTime() == null) {
            throw new GeneralException(Constants.FROM_DATE_TIME_INVALID);
        }
        ZonedDateTime fromTime = null;
        try {
            fromTime = parse(request.getFromTime());
        } catch (DateTimeParseException e) {
            throw new InvalidFormatException("fromTime");
        }
        stopOrderList = stopOrderRepo.findStopOrderLastUpdate(fromTime);
        List<StopOrderHistoryResponse> responses = new ArrayList<>();
        stopOrderList.forEach(stopOrder -> responses.add(StopOrderHistoryResponse.fromStopOrder(stopOrder, objectMapper)));
        return CompletableFuture.completedFuture(responses);
    }

    private ZonedDateTime parseAtAndDefault(String date, ZonedDateTime defaultValue) {
        return StringUtils.isEmpty(date) ? defaultValue : parse(date);
    }

    private ZonedDateTime parse(String date) {
        return DefaultUtils.parseZonedDate(date).truncatedTo(ChronoUnit.DAYS);
    }
}
