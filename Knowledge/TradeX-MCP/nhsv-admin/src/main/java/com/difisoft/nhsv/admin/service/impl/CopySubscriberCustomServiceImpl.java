package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.kafka.Message;
import com.difisoft.model.requests.Headers;
import com.difisoft.model.requests.Token;
import com.difisoft.model.responses.MessageResponse;
import com.difisoft.model.responses.Response;
import com.difisoft.nhsv.admin.config.AppConf;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.*;
import com.difisoft.nhsv.admin.domain.enumeration.OrderSetTypeEnum;
import com.difisoft.nhsv.admin.domain.request.*;
import com.difisoft.nhsv.admin.domain.response.AccountInfoResponse;
import com.difisoft.nhsv.admin.domain.response.GenericResponse;
import com.difisoft.nhsv.admin.domain.response.MarketLeaderSubGrowthRateResponse;
import com.difisoft.nhsv.admin.domain.response.SubscriberInformationResponse;
import com.difisoft.nhsv.admin.domain.response.SubscriberInformationResponse.MarketLeaderinfo;
import com.difisoft.nhsv.admin.repository.*;
import com.difisoft.nhsv.admin.security.AuthoritiesConstants;
import com.difisoft.nhsv.admin.service.*;
import com.difisoft.nhsv.admin.service.criteria.CopySubscriberCriteria;
import com.difisoft.nhsv.admin.service.criteria.CopySubscriberHistoryCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDTO;
import com.difisoft.nhsv.admin.service.mapper.CopySubscriberMapper;
import com.difisoft.nhsv.admin.utils.DateTimeUtil;
import com.difisoft.nhsv.admin.utils.Util;
import com.difisoft.redis.RedisDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.filter.StringFilter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("copySubscriberCustomService")
@Transactional
@Slf4j
@Primary
public class CopySubscriberCustomServiceImpl extends CopySubscriberServiceImpl implements CopySubscriberCustomService {
    private final CopySubscriberCustomRepository copySubscriberCustomRepository;
    private final CopySubscriberMapper copySubscriberMapper;
    private final UserCustomRepository userCustomRepository;
    private final CopySubscriberHistoryCustomRepository copySubscriberHistoryRepository;
    private final CopySubscriberCustomQueryService copySubscriberQueryService;
    private final CopySubscriberHistoryCustomQueryService copySubscriberHistoryQueryService;
    private final CopyUserService copyUserService;
    private final CommonService commonService;
    private final CopyMarketLeaderDetailsCustomService copyMarketLeaderDetailsCustomService;
    private final ObjectMapper objectMapper;
    private final CopySubscriberDetailsCustomService copySubscriberDetailsCustomService;
    private final RedisDao redisDao;
    private final CopyMarketLeaderProfitLossCustomRepository copyMarketLeaderProfitLossCustomRepository;
    private final RedisDaoExtend redisDaoExtend;
    private final AppConf appConf;
    private final CopyTradingRegisterCustomRepository copyTradingRegisterCustomRepository;


    public CopySubscriberCustomServiceImpl(
            CopySubscriberRepository copySubscriberRepository, CopySubscriberMapper copySubscriberMapper,
            CopySubscriberCustomRepository copySubscriberCustomRepository, AppConf appConf,
            UserCustomRepository userCustomRepository,
            CopySubscriberHistoryCustomRepository copySubscriberHistoryRepository,
            CopySubscriberCustomQueryService copySubscriberQueryService,
            CopySubscriberHistoryCustomQueryService copySubscriberHistoryQueryService,
            CopyUserService copyUserService,
            CommonService commonService, CopyMarketLeaderDetailsCustomService copyMarketLeaderDetailsCustomService,
            ObjectMapper objectMapper, CopySubscriberDetailsCustomService copySubscriberDetailsCustomService,
            CopyMarketLeaderProfitLossCustomRepository copyMarketLeaderProfitLossCustomRepository,
            RedisDao redisDao,
            RedisDaoExtend redisDaoExtend, CopyTradingRegisterCustomRepository copyTradingRegisterCustomRepository) {
        super(copySubscriberRepository, copySubscriberMapper);
        this.copySubscriberCustomRepository = copySubscriberCustomRepository;
        this.copySubscriberMapper = copySubscriberMapper;
        this.userCustomRepository = userCustomRepository;
        this.copySubscriberHistoryRepository = copySubscriberHistoryRepository;
        this.copySubscriberQueryService = copySubscriberQueryService;
        this.copySubscriberHistoryQueryService = copySubscriberHistoryQueryService;
        this.copyUserService = copyUserService;
        this.commonService = commonService;
        this.copyMarketLeaderDetailsCustomService = copyMarketLeaderDetailsCustomService;
        this.objectMapper = objectMapper;
        this.copySubscriberDetailsCustomService = copySubscriberDetailsCustomService;
        this.redisDao = redisDao;
        this.copyMarketLeaderProfitLossCustomRepository = copyMarketLeaderProfitLossCustomRepository;
        this.appConf = appConf;
        this.redisDaoExtend = redisDaoExtend;
        this.copyTradingRegisterCustomRepository = copyTradingRegisterCustomRepository;
    }

    @Override
    public Page<CopySubscriberDTO> findAllByMlId(Long mlUserId, Pageable pageable) {
        Page<CopySubscriber> copySubscribers = copySubscriberCustomRepository.findAllByMlUserId(mlUserId, pageable);
        log.info("[findAllByMlId] copyMarketLeaderDetails: {}", copySubscribers);
        return copySubscribers.map(copySubscriberMapper::toDto);
    }

    @Override
    public GenericResponse<MarketLeaderSubGrowthRateResponse> findMarketLeaderSubscriberGrowthRate(
        MarketLeaderSubGrowthRateRequest request, RequestContext<MarketLeaderSubGrowthRateRequest> ctx) {
        String ctxId = ctx.getId();
        GenericResponse<MarketLeaderSubGrowthRateResponse> response;
        int pageSize = request.buildDefaultPageSize(request.getPageSize());
        int pageNumber = request.buildDefaultPageNumber(request.getPageNumber());
        boolean requestSortAsc = request.buildDefaultSortAsc(request.getSortAsc());
        String methodName = "findMarketLeaderSubscriberGrowthRate";
        String prefixLog = String.format("%s -- ctxId: %s", methodName, ctxId);
        try {
            response = GenericResponse.success("");
            log.info("{} -- request: {}, ctx: {}", prefixLog, request, ctx);

            // Get cache
            String key = String.format("%s_%s_%s"
                , Constants.CacheNames.EXPIRED_IN_JOB_CLEAR, methodName, request.objToString());
            if (redisDaoExtend.isExists(key)) {
                String cacheData = redisDaoExtend.get(key, String.class);
                response = commonService.getObjectMapper().readValue(cacheData, new TypeReference<>() {
                });
                log.info("{} -- cache response: {}", prefixLog, Util.objectToStringJsonIgnoreError(response));
                return response;
            }

            // Validate
            commonService.validateMarketLeaderRequired(request.getMarketLeaderId());
            List<Long> mlUserActiveIds = this.copyUserService
                .findAllByIdsAndTypeAndStatus(List.of(request.getMarketLeaderId()),
                    AuthoritiesConstants.MARKET_LEADER, Boolean.TRUE)
                .stream().map(User::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(mlUserActiveIds)) {
                throw new GeneralException(
                    Constants.INVALID_MARKET_LEADER,
                    MessageFormat.format(Constants.INACTIVE_MARKET_LEADER_ID_MSG, request.getMarketLeaderId()));
            }
            ZonedDateTime fromDate = DateTimeUtil.toStartOfDay(
                DateTimeUtil.stringToZoneDateTime(request.getFromDate(), Constants.DATE_FORMAT_yyyyMMdd,
                    Constants.DateTimeType.DATE));
            ZonedDateTime toDate = DateTimeUtil.toEndOfDay(
                DateTimeUtil.stringToZoneDateTime(request.getToDate(), Constants.DATE_FORMAT_yyyyMMdd,
                    Constants.DateTimeType.DATE));
            log.info("{} -- fromDate: {}, toDate: {}", prefixLog, fromDate, toDate);
            if (Objects.nonNull(fromDate) && fromDate.toLocalDate().isAfter(LocalDate.now())) {
                log.info("{} -- From date must be before today. Input value is: {}", prefixLog, request.getFromDate());
                response = GenericResponse
                    .badRequest("From date must be before today. Input value is: " + request.getFromDate());
                return response;
            }

            // CALC subscribers growth rate
            MarketLeaderSubGrowthRateResponse data = new MarketLeaderSubGrowthRateResponse();
            Pageable pageable = PageRequest.of(pageNumber, pageSize, requestSortAsc
                ? Sort.by(Sort.Direction.ASC, Constants.CREATED_AT)
                : Sort.by(Sort.Direction.DESC, Constants.CREATED_AT));
            Page<CopyMarketLeaderDetails> page = this.copyMarketLeaderDetailsCustomService
                .findAllByMlIdsAndDateRangeAndConditions(
                    List.of(request.getMarketLeaderId()), fromDate, toDate,
                    Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING,
                    Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO,
                    Constants.CopyMarketLeaderDetailConstants.KEY_TOTAL_SUB, pageable);
            log.info("{} -- copyMarketLeaderDetailsPage: {}", prefixLog, Util.objectToStringJsonIgnoreError(page));

            List<MarketLeaderSubGrowthRateResponse.MarketLeaderSubGrowthRateItem> growthRateItems = page.getContent()
                .stream().map(totalSubInfo -> {
                    MarketLeaderSubGrowthRateResponse.MarketLeaderSubGrowthRateItem item = new MarketLeaderSubGrowthRateResponse.MarketLeaderSubGrowthRateItem();
                    item.setDate(
                        DateTimeUtil.zonedDateTimeToString(totalSubInfo.getCreatedAt(), Constants.DATE_FORMAT_PATTERN_1)
                    );
                    item.setTotalSubscribers(
                        commonService.convertTotalSubStrToLong(totalSubInfo.getValue(), ctxId));
                    return item;
                }).collect(Collectors.toList());
            log.info("{} -- growthRateItems: {}", prefixLog, Util.objectToStringJsonIgnoreError(growthRateItems));

            // Response
            data.setBeMarketLeaderDate(
                DateTimeUtil.zonedDateTimeToString(commonService.getBeMarketLeaderDateByMlId(request.getMarketLeaderId()), Constants.DATE_FORMAT_PATTERN_1)
            );
            data.setItems(growthRateItems);
            response.setData(data);
            GenericResponse.buildingPageData(response, page);

            // Set cache
            if (!redisDaoExtend.isExists(key)) {
                redisDaoExtend.set(key, commonService.objectToStringJson(response));
            }

        } catch (GeneralException e) {
            log.error(Util.objectToStringJsonIgnoreError(e.getStackTrace()));
            throw e;
        } catch (Exception e) {
            if (Boolean.TRUE.equals(appConf.getIsEnableApiResponseDefault())) {
                throw new RuntimeException(e);
            }
            log.info("{} -- error", prefixLog, e);
            response = GenericResponse.internalServerError(e.getMessage());
        }
        log.info("{} -- response: {}", prefixLog, Util.objectToStringJsonIgnoreError(response));
        return response;
    }

    @Override
    public MessageResponse subscribe(SubscribeRequest request, RequestContext<SubscribeRequest> ctx) {
        log.info("[subscribe] ctx: {}, request: {}", ctx.getId(), request);
        MessageResponse response = new MessageResponse();
        List<String> accountNumbers = request.getAccountNumbers();
        String tokenUsername = request.getTokenUsername();
        if (Objects.isNull(request.getUsername())) {
            log.info("[subscribe] ctxId: {}, Param username is null", ctx.getId());
            throw new GeneralException(Constants.USERNAME_IS_REQUIRED);
        }
        if (Objects.isNull(request.getAccountNumber())) {
            log.info("[subscribe] ctxId: {}, Param accountNumber is null", ctx.getId());
            throw new GeneralException(Constants.ACCOUNT_NUMBER_IS_REQUIRED);
        }
        if (Objects.isNull(request.getSubNumber())) {
            log.info("[subscribe] ctxId: {}, Param subNumber is null", ctx.getId());
            throw new GeneralException(Constants.SUB_NUMBER_IS_REQUIRED);
        }
        if (Objects.isNull(request.getMarketLeaderId())) {
            log.info("[subscribe] ctxId: {}, Param marketLeaderId is null", ctx.getId());
            throw new GeneralException(Constants.MARKET_LEADER_ID_IS_REQUIRED);
        }
        if (Objects.isNull(request.getOrderSetType())) {
            log.info("[subscribe] ctxId: {}, Param orderSetType is null", ctx.getId());
            throw new GeneralException(Constants.ORDER_SET_TYPE_IS_REQUIRED);
        }
        if (Objects.isNull(request.getAllocatedRatio())) {
            log.info("[subscribe] ctxId: {}, Param allocatedRatio is null", ctx.getId());
            throw new GeneralException(Constants.ALLOCATED_RATIO_IS_REQUIRED);
        }
        if (Objects.isNull(request.getDeviceUniqueId())) {
            log.info("[subscribe] ctxId: {}, Param deviceUniqueId is null", ctx.getId());
            throw new GeneralException(Constants.DEVICE_UNIQUE_ID_IS_REQUIRED);
        }
        long count = copyTradingRegisterCustomRepository.countByAccountNumberAndSubAccount(request.getAccountNumber(), request.getSubNumber());
        if (count == 0) {
            throw new GeneralException(Constants.SUB_ACCOUNT_NOT_YET_REGISTER);
        }
        if (tokenUsername == null || tokenUsername.isEmpty() || !tokenUsername.equals(request.getUsername())) {
            throw new GeneralException(Constants.INVALID_USERNAME);
        }
        if (CollectionUtils.isEmpty(accountNumbers) || !CollectionUtils.containsAny(accountNumbers, request.getAccountNumber())) {
            throw new GeneralException(Constants.INVALID_ACCOUNT_NUMBER);
        }
        User uInfo = userCustomRepository.findById(request.getMarketLeaderId())
            .orElseThrow(() -> new GeneralException(Constants.INVALID_MARKET_LEADER));
        if (!uInfo.isActivated()) {
            throw new GeneralException(Constants.INVALID_MARKET_LEADER);
        }
        CopySubscriber copySubscriber = copySubscriberCustomRepository.findByAccountNumberAndSubNumberAndUserName(
                request.getAccountNumber(),
                request.getSubNumber(),
                request.getUsername())
            .orElse(null);
        String key = "copySubscriber_" + request.getAccountNumber() + "_" + request.getSubNumber() + "_"
            + request.getUsername();
        // check redis
        Boolean isContinue = redisDao.get(key, Boolean.class);
        if (copySubscriber != null || isContinue != null) {
            throw new GeneralException(Constants.SUB_NUMBER_HAS_BEEN_SUBSCRIBED_BEFORE);
        }
        if (request.getAllocatedRatio() <= 0 || request.getAllocatedRatio() > 1) {
            throw new GeneralException(Constants.INVALID_ALLOCATED_RATIO);
        }
        OrderSetTypeEnum orderSetType;
        try {
            orderSetType = OrderSetTypeEnum.valueOf(request.getOrderSetType());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(Constants.INVALID_ORDER_SET_TYPE);
        }
        CopySubscriber newCopySubscriber = new CopySubscriber();
        newCopySubscriber.setMlUserId(uInfo);
        newCopySubscriber.setAccountNumber(request.getAccountNumber());
        newCopySubscriber.setSubNumber(request.getSubNumber());
        newCopySubscriber.setUserName(request.getUsername());
        newCopySubscriber.setAllocatedRatio(request.getAllocatedRatio());
        newCopySubscriber.setOrderSetType(orderSetType);
        newCopySubscriber.setCreatedAt(ZonedDateTime.now());
        newCopySubscriber.setUpdatedAt(ZonedDateTime.now());
        newCopySubscriber.setDeviceUniqueId(request.getDeviceUniqueId());
        newCopySubscriber.setCustomerName(request.getHeaders().getToken().getUserData().getName());
        CopySubscriber savedCopySubscriber;
        try {
            savedCopySubscriber = copySubscriberCustomRepository.save(newCopySubscriber);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(Constants.SUB_NUMBER_HAS_BEEN_SUBSCRIBED_BEFORE);
        }
        // save to redis
        try {
            redisDao.set(key, true, 60);
        } catch (JsonProcessingException e) {
            log.error("[subscribe] redis set error: ", e);
        }
        if (Objects.nonNull(savedCopySubscriber.getId())) {
            response.setMessage(Constants.COPY_TRADE_SUBSCRIBE_SUCCESS);
            Token.UserData userData = request.getHeaders().getToken().getUserData();
            CopySubscriberDetails subscriberDetail = this.copySubscriberDetailsCustomService
                .findBySubscriberId(savedCopySubscriber.getId()).orElse(new CopySubscriberDetails());
            subscriberDetail.setUsername(userData.getUsername());
            subscriberDetail.setIdentifierNumber(userData.getIdentifierNumber());
            subscriberDetail.setBranchCode(userData.getBranchCode());
            subscriberDetail.setMngDeptCode(userData.getMngDeptCode());
            subscriberDetail.setDeptCode(userData.getDeptCode());
            subscriberDetail.setAgencyNumber(userData.getAgencyNumber());
            subscriberDetail.setAccountNumbers(accountNumbers.get(0));
            subscriberDetail.setCopySubscriberId(savedCopySubscriber);
            CopySubscriberDetails subDetailResult = copySubscriberDetailsCustomService.save(subscriberDetail);
            log.info("[subscribe] ctxId: {}, subDetailResult: {}", ctx.getId(), subDetailResult);
        }
        return response;
    }

    @Override
    public MessageResponse unSubscribe(UnSubscribeRequest request, RequestContext<UnSubscribeRequest> ctx) {
        log.info("[unSubscribe] ctx: {}, request: {}", ctx.getId(), request);
        MessageResponse response = new MessageResponse();
        List<String> accountNumbers = request.getAccountNumbers();
        String tokenUsername = request.getTokenUsername();
        if (Objects.isNull(request.getUsername())) {
            log.info("[unSubscribe] ctxId: {}, Param username is null", ctx.getId());
            throw new GeneralException(Constants.USERNAME_IS_REQUIRED);
        }
        if (Objects.isNull(request.getAccountNumber())) {
            log.info("[unSubscribe] ctxId: {}, Param accountNumber is null", ctx.getId());
            throw new GeneralException(Constants.ACCOUNT_NUMBER_IS_REQUIRED);
        }
        if (Objects.isNull(request.getSubNumber())) {
            log.info("[unSubscribe] ctxId: {}, Param subNumber is null", ctx.getId());
            throw new GeneralException(Constants.SUB_NUMBER_IS_REQUIRED);
        }
        if (tokenUsername == null || tokenUsername.isEmpty() || !tokenUsername.equals(request.getUsername())) {
            throw new GeneralException(Constants.INVALID_USERNAME);
        }
        if (CollectionUtils.isEmpty(accountNumbers) || !CollectionUtils.containsAny(accountNumbers, request.getAccountNumber())) {
            throw new GeneralException(Constants.INVALID_ACCOUNT_NUMBER);
        }
        CopySubscriber copySubscriber = copySubscriberCustomRepository.findByAccountNumberAndSubNumberAndUserName(
                request.getAccountNumber(),
                request.getSubNumber(),
                request.getUsername())
            .orElseThrow(() -> new GeneralException(Constants.SUB_NUMBER_HAS_NOT_BEEN_SUBSCRIBED_YET));
        CopySubscriberHistory newCopySubscriberHistory = new CopySubscriberHistory();
        newCopySubscriberHistory.setMlUserId(copySubscriber.getMlUserId());
        newCopySubscriberHistory.setId(copySubscriber.getId());
        newCopySubscriberHistory.setAccountNumber(copySubscriber.getAccountNumber());
        newCopySubscriberHistory.setSubNumber(copySubscriber.getSubNumber());
        newCopySubscriberHistory.setUserName(copySubscriber.getUserName());
        newCopySubscriberHistory.setAllocatedRatio(copySubscriber.getAllocatedRatio());
        newCopySubscriberHistory.setOrderSetType(copySubscriber.getOrderSetType());
        newCopySubscriberHistory.setCreatedAt(copySubscriber.getCreatedAt());
        newCopySubscriberHistory.setUpdatedAt(ZonedDateTime.now());
        CopySubscriberHistory savedCopySubscriberHistory = copySubscriberHistoryRepository
            .save(newCopySubscriberHistory);
        if (Objects.nonNull(savedCopySubscriberHistory.getId())) {
            this.copySubscriberDetailsCustomService
                .findBySubscriberId(copySubscriber.getId())
                .ifPresent(x -> this.copySubscriberDetailsCustomService.delete(x.getId()));
            copySubscriberCustomRepository.delete(copySubscriber);
            response.setMessage(Constants.COPY_TRADE_UNSUBSCRIBE_SUCCESS);
        }
        return response;
    }

    @Override
    public GenericResponse<List<SubscriberInformationResponse>> findSubscriberInformation(
        SubscriberInformationRequest request, RequestContext<SubscriberInformationRequest> ctx) {
        log.info("[findSubscriberInformation] ctx: {}, request: {}", ctx.getId(), request);
        GenericResponse<List<SubscriberInformationResponse>> response = new GenericResponse<>();
        try {
            List<String> accountNumbers = request.getAccountNumbers();
            int pageSize = request.buildDefaultPageSize(request.getPageSize());
            int pageNumber = request.buildDefaultPageNumber(request.getPageNumber());
            boolean isSortAsc = request.getSortAsc();
            if (Objects.isNull(request.getAccountNumber())) {
                log.info("[unSubscribe] ctxId: {}, Param accountNumber is null", ctx.getId());
                throw new GeneralException(Constants.ACCOUNT_NUMBER_IS_REQUIRED);
            }
            if (accountNumbers == null || accountNumbers.isEmpty()
                || !accountNumbers.contains(request.getAccountNumber())) {
                throw new GeneralException(Constants.INVALID_ACCOUNT_NUMBER);
            }
            response = GenericResponse.success("");
            StringFilter accountNumberFilter = new StringFilter();
            accountNumberFilter.setEquals(request.getAccountNumber());
            StringFilter usernameFilter = new StringFilter();
            usernameFilter.setEquals(request.getUsername());
            StringFilter subNumberFilter = new StringFilter();
            if (request.getSubNumber() != null && !request.getSubNumber().isEmpty()) {
                subNumberFilter.setEquals(request.getSubNumber());
            }
            Sort sort = isSortAsc ? Sort.by(Sort.Direction.ASC, Constants.UPDATED_AT)
                : Sort.by(Sort.Direction.DESC, Constants.UPDATED_AT);
            Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
            List<SubscriberInformationResponse> subscriberInformationResponses = new ArrayList<>();
            Page<CopySubscriber> copySubscribers;
            Page<CopySubscriberHistory> copySubscribersHistory;
            Page<SubscriberInformationResponse> page;
            Pageable pageableProfitLoss = PageRequest.of(Constants.DEFAULT_OFFSET, Constants.PAGE_SIZE_1,
                Sort.by(Sort.Direction.DESC, Constants.CREATED_AT));
            if (request.getCopyTradingStatus() == null || request.getCopyTradingStatus().isEmpty()) {
                CopySubscriberCriteria criteria = new CopySubscriberCriteria();
                criteria.setAccountNumber(accountNumberFilter);
                criteria.setUserName(usernameFilter);
                if (request.getSubNumber() != null && !request.getSubNumber().isEmpty()) {
                    criteria.setSubNumber(subNumberFilter);
                }

                List<CopySubscriber> copySubscriberList = copySubscriberQueryService.findByCriteria(criteria, sort);
                if (!copySubscriberList.isEmpty()) {
                    copySubscriberList.forEach(copySubscriberDTO -> {
                        List<CopyMarketLeaderProfitLoss> copyMarketLeaderProfitLossList = copyMarketLeaderProfitLossCustomRepository
                            .findByMlUserIdAndType(copySubscriberDTO.getMlUserId().getId(), Constants.MONTH,
                                pageableProfitLoss);
                        SubscriberInformationResponse data = SubscriberInformationResponse.builder()
                            .username(copySubscriberDTO.getUserName())
                            .accountNumber(copySubscriberDTO.getAccountNumber())
                            .subNumber(copySubscriberDTO.getSubNumber())
                            .marketLeaderInfo(MarketLeaderinfo.builder()
                                .marketLeaderId(copySubscriberDTO.getMlUserId().getId())
                                .marketLeaderUsername(copySubscriberDTO.getMlUserId().getLogin())
                                .marketLeaderFullname(copySubscriberDTO.getMlUserId().getFullName())
                                .marketLeaderStatus(
                                    copySubscriberDTO.getMlUserId().isActivated() ? "ACTIVE"
                                        : "INACTIVE")
                                .marketLeaderImageUrl(copySubscriberDTO.getMlUserId().getPhoto())
                                .profitLossRatio(copyMarketLeaderProfitLossList.isEmpty() ? 0
                                    : copyMarketLeaderProfitLossList.get(0).getProfitLossRatio())
                                .totalSubscribers(
                                    commonService.convertTotalSubStrToLong(this.getTotalSubscribers(
                                        copySubscriberDTO.getMlUserId().getId()), ctx.getId()))
                                .build())
                            .subscribedDateTime(
                                copySubscriberDTO.getCreatedAt().format(
                                    DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)))
                            .copyTradingStatus("SUBSCRIBE")
                            .build();
                        subscriberInformationResponses.add(data);
                    });
                }
                CopySubscriberHistoryCriteria historyCriteria = new CopySubscriberHistoryCriteria();
                historyCriteria.setAccountNumber(accountNumberFilter);
                historyCriteria.setUserName(usernameFilter);
                if (request.getSubNumber() != null && !request.getSubNumber().isEmpty()) {
                    historyCriteria.setSubNumber(subNumberFilter);
                }
                List<CopySubscriberHistory> copySubscribersHistoryList = copySubscriberHistoryQueryService
                    .findByCriteria(
                        historyCriteria, sort);
                if (!copySubscribersHistoryList.isEmpty()) {
                    copySubscribersHistoryList.forEach(copySubscriberHistoryDTO -> {
                        List<CopyMarketLeaderProfitLoss> copyMarketLeaderProfitLossList = copyMarketLeaderProfitLossCustomRepository
                            .findByMlUserIdAndType(copySubscriberHistoryDTO.getMlUserId().getId(), Constants.MONTH,
                                pageableProfitLoss);
                        SubscriberInformationResponse data = SubscriberInformationResponse.builder()
                            .username(copySubscriberHistoryDTO.getUserName())
                            .accountNumber(copySubscriberHistoryDTO.getAccountNumber())
                            .subNumber(copySubscriberHistoryDTO.getSubNumber())
                            .marketLeaderInfo(MarketLeaderinfo.builder()
                                .marketLeaderId(copySubscriberHistoryDTO.getMlUserId().getId())
                                .marketLeaderUsername(copySubscriberHistoryDTO.getMlUserId().getLogin())
                                .marketLeaderFullname(copySubscriberHistoryDTO.getMlUserId().getFullName())
                                .marketLeaderStatus(
                                    copySubscriberHistoryDTO.getMlUserId().isActivated() ? "ACTIVE"
                                        : "INACTIVE")
                                .marketLeaderImageUrl(copySubscriberHistoryDTO.getMlUserId().getPhoto())
                                .profitLossRatio(copyMarketLeaderProfitLossList.isEmpty() ? 0
                                    : copyMarketLeaderProfitLossList.get(0).getProfitLossRatio())
                                .totalSubscribers(
                                    commonService.convertTotalSubStrToLong(this.getTotalSubscribers(
                                        copySubscriberHistoryDTO.getMlUserId().getId()), ctx.getId()))
                                .build())
                            .subscribedDateTime(copySubscriberHistoryDTO.getCreatedAt().format(
                                DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)))
                            .unsubscribedDateTime(copySubscriberHistoryDTO.getUpdatedAt().format(
                                DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)))
                            .copyTradingStatus("UNSUBSCRIBE")
                            .build();
                        subscriberInformationResponses.add(data);
                    });
                }
            } else if (request.getCopyTradingStatus().equals("SUBSCRIBE")) {
                CopySubscriberCriteria criteria = new CopySubscriberCriteria();
                criteria.setAccountNumber(accountNumberFilter);
                criteria.setUserName(usernameFilter);
                if (request.getSubNumber() != null && !request.getSubNumber().isEmpty()) {
                    criteria.setSubNumber(subNumberFilter);
                }
                copySubscribers = copySubscriberQueryService.findByCriteria(criteria, pageable);
                if (!copySubscribers.getContent().isEmpty()) {
                    copySubscribers.getContent().forEach(copySubscriberDTO -> {
                        List<CopyMarketLeaderProfitLoss> copyMarketLeaderProfitLossList = copyMarketLeaderProfitLossCustomRepository
                            .findByMlUserIdAndType(copySubscriberDTO.getMlUserId().getId(), Constants.MONTH,
                                pageableProfitLoss);
                        SubscriberInformationResponse data = SubscriberInformationResponse.builder()
                            .username(copySubscriberDTO.getUserName())
                            .accountNumber(copySubscriberDTO.getAccountNumber())
                            .subNumber(copySubscriberDTO.getSubNumber())
                            .marketLeaderInfo(MarketLeaderinfo.builder()
                                .marketLeaderId(copySubscriberDTO.getMlUserId().getId())
                                .marketLeaderUsername(copySubscriberDTO.getMlUserId().getLogin())
                                .marketLeaderFullname(copySubscriberDTO.getMlUserId().getFullName())
                                .marketLeaderStatus(
                                    copySubscriberDTO.getMlUserId().isActivated() ? "ACTIVE"
                                        : "INACTIVE")
                                .marketLeaderImageUrl(copySubscriberDTO.getMlUserId().getPhoto())
                                .profitLossRatio(copyMarketLeaderProfitLossList.isEmpty() ? 0
                                    : copyMarketLeaderProfitLossList.get(0).getProfitLossRatio())
                                .totalSubscribers(
                                    commonService.convertTotalSubStrToLong(this.getTotalSubscribers(
                                        copySubscriberDTO.getMlUserId().getId()), ctx.getId()))

                                .build())
                            .subscribedDateTime(copySubscriberDTO.getCreatedAt().format(
                                DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)))
                            .copyTradingStatus("SUBSCRIBE")
                            .build();
                        subscriberInformationResponses.add(data);
                    });
                }
            } else if (request.getCopyTradingStatus().equals("UNSUBSCRIBE")) {
                CopySubscriberHistoryCriteria criteria = new CopySubscriberHistoryCriteria();
                criteria.setAccountNumber(accountNumberFilter);
                criteria.setUserName(usernameFilter);
                if (request.getSubNumber() != null && !request.getSubNumber().isEmpty()) {
                    criteria.setSubNumber(subNumberFilter);
                }
                copySubscribersHistory = copySubscriberHistoryQueryService.findByCriteria(
                    criteria,
                    pageable);
                if (!copySubscribersHistory.getContent().isEmpty()) {
                    copySubscribersHistory.getContent().forEach(copySubscriberHistoryDTO -> {
                        List<CopyMarketLeaderProfitLoss> copyMarketLeaderProfitLossList = copyMarketLeaderProfitLossCustomRepository
                            .findByMlUserIdAndType(copySubscriberHistoryDTO.getMlUserId().getId(), Constants.MONTH,
                                pageableProfitLoss);
                        SubscriberInformationResponse data = SubscriberInformationResponse.builder()
                            .username(copySubscriberHistoryDTO.getUserName())
                            .accountNumber(copySubscriberHistoryDTO.getAccountNumber())
                            .subNumber(copySubscriberHistoryDTO.getSubNumber())
                            .marketLeaderInfo(MarketLeaderinfo.builder()
                                .marketLeaderId(copySubscriberHistoryDTO.getMlUserId().getId())
                                .marketLeaderUsername(copySubscriberHistoryDTO.getMlUserId().getLogin())
                                .marketLeaderFullname(copySubscriberHistoryDTO.getMlUserId().getFullName())
                                .marketLeaderStatus(
                                    copySubscriberHistoryDTO.getMlUserId().isActivated() ? "ACTIVE"
                                        : "INACTIVE")
                                .marketLeaderImageUrl(copySubscriberHistoryDTO.getMlUserId().getPhoto())
                                .profitLossRatio(copyMarketLeaderProfitLossList.isEmpty() ? 0
                                    : copyMarketLeaderProfitLossList.get(0).getProfitLossRatio())
                                .totalSubscribers(
                                    commonService.convertTotalSubStrToLong(this.getTotalSubscribers(
                                        copySubscriberHistoryDTO.getMlUserId().getId()), ctx.getId()))
                                .build())
                            .subscribedDateTime(copySubscriberHistoryDTO.getCreatedAt().format(
                                DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)))
                            .unsubscribedDateTime(copySubscriberHistoryDTO.getUpdatedAt().format(
                                DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)))
                            .copyTradingStatus("UNSUBSCRIBE")
                            .build();
                        subscriberInformationResponses.add(data);
                    });
                }
            }
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), subscriberInformationResponses.size());
            page = new PageImpl<>(subscriberInformationResponses.subList(start, end), pageable,
                subscriberInformationResponses.size());
            response.setData(page.getContent());
            response.setPageData(
                GenericResponse.PageData.builder()
                    .pageSize(page.getSize()).pageNumber(page.getNumber())
                    .totalPages(page.getTotalPages()).totalElements(page.getTotalElements())
                    .build());
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            if (Boolean.TRUE.equals(appConf.getIsEnableApiResponseDefault())) {
                throw e;
            }
            log.error("[findSubscriberInformation] ctxId: {}", ctx.getId(), e);
            response = GenericResponse.internalServerError(e.getMessage());
        }
        return response;
    }

    private String getTotalSubscribers(Long id) {
        List<CopyMarketLeaderDetailsDTO> mlDetails = copyMarketLeaderDetailsCustomService
            .findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(
                List.of(id), Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING,
                Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO,
                Constants.CopyMarketLeaderDetailConstants.KEY_TOTAL_SUB,
                Sort.by(Constants.CREATED_AT).descending());
        if (CollectionUtils.isEmpty(mlDetails)) {
            return "0";
        } else {
            return mlDetails.get(0).getValue();
        }
    }

    private boolean checkSubAccount(Headers headers, String msgId, String accountNumber, String subNumber) {
        log.info("[checkSubAccount] msgId: {}, accountNumber: {}, subNumber: {}", msgId, accountNumber, subNumber);
        try {
            String uri = "/api/v1/equity/account/info";
            AccountInfoRequest request = new AccountInfoRequest();
            request.setAccountNumber(accountNumber);
            request.setSubNumber(subNumber);
            request.setHeaders(headers);
            Message msg = commonService.createKafkaRequest("tuxedo", uri, request, "accountInfo");
            AccountInfoResponse response = (AccountInfoResponse) msg.getResponse(objectMapper,
                new TypeReference<Response<AccountInfoResponse>>() {
                });
            if (response == null) {
                log.error("[checkSubAccount] msgId: {}, response is null", msgId);
                return false;
            }
            return response.getUsername().equals(accountNumber);
        } catch (Exception e) {
            log.error("[checkSubAccount] msgId: {}, error: {}", msgId, e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
