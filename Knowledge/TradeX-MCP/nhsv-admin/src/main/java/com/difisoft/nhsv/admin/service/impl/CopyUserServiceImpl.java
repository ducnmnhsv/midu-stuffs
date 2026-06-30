package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.file.FileService;
import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.nhsv.admin.config.AppConf;
import com.difisoft.nhsv.admin.config.ApplicationProperties;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.*;
import com.difisoft.nhsv.admin.domain.enumeration.PeriodEnum;
import com.difisoft.nhsv.admin.domain.request.*;
import com.difisoft.nhsv.admin.domain.response.*;
import com.difisoft.nhsv.admin.domain.response.CurrentPorfolioResponse.CurrentPorfolio;
import com.difisoft.nhsv.admin.repository.*;
import com.difisoft.nhsv.admin.security.AuthoritiesConstants;
import com.difisoft.nhsv.admin.service.*;
import com.difisoft.nhsv.admin.service.dto.AdminUserDTO;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
import com.difisoft.nhsv.admin.utils.Util;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("copyUserService")
@Slf4j
public class CopyUserServiceImpl extends UserService implements CopyUserService {
    private final CopyMarketLeaderDetailsCustomService copyMarketLeaderDetailsCustomService;
    private final CopyPortfolioDetailsCustomRepository copyPortfolioDetailsRepository;
    private final UserCustomRepository userCustomRepository;
    private final CopyPortfolioHistoryCustomRepository copyPortfolioHistoryRepository;
    private final CopyPortfolioDetailHistoryCustomRepository copyPortfolioDetailsHistoryRepository;
    private final CommonService commonService;
    private final ApplicationProperties appConf;
    private final RedisDaoExtend redisDaoExtend;
    private final AppConf propConf;
    private final BrokerRepository brokerRepository;

    @Autowired
    public CopyUserServiceImpl(
            UserRepository userRepository, PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository,
            FileService fileService,
            CopyMarketLeaderDetailsCustomService copyMarketLeaderDetailsCustomService,
            CopyPortfolioDetailsCustomRepository copyPortfolioDetailsRepository,
            UserCustomRepository userCustomRepository,
            CopyPortfolioHistoryCustomRepository copyPortfolioHistoryRepository,
            CopyPortfolioDetailHistoryCustomRepository copyPortfolioDetailsHistoryRepository,
            CommonService commonService,
            ApplicationProperties appConf,
            RedisDaoExtend redisDaoExtend,
            AppConf propConf,
            BrokerRepository brokerRepository
    ) {
        super(userRepository, passwordEncoder, authorityRepository, fileService, brokerRepository);
        this.copyMarketLeaderDetailsCustomService = copyMarketLeaderDetailsCustomService;
        this.copyPortfolioDetailsRepository = copyPortfolioDetailsRepository;
        this.userCustomRepository = userCustomRepository;
        this.copyPortfolioHistoryRepository = copyPortfolioHistoryRepository;
        this.copyPortfolioDetailsHistoryRepository = copyPortfolioDetailsHistoryRepository;
        this.commonService = commonService;
        this.appConf = appConf;
        this.redisDaoExtend = redisDaoExtend;
        this.propConf = propConf;
        this.brokerRepository = brokerRepository;
    }

    @Override
    @Transactional
    public AdminUserDTO findMLAccountInfo(Long userId) {
        AdminUserDTO mlUserDto;
        if (Objects.nonNull(userId)) {
            mlUserDto = findById(userId).map(AdminUserDTO::new)
                .orElseThrow(() -> new GeneralException("User could not be found, ID = " + userId));
        } else {
            mlUserDto = getUserWithAuthorities().map(AdminUserDTO::new)
                .orElseThrow(() -> new GeneralException("User could not be found, ID = " + userId));
        }
        List<CopyMarketLeaderDetailsDTO> mlDetails = copyMarketLeaderDetailsCustomService
            .findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(
                List.of(mlUserDto.getId()), Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING,
                Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO,
                Constants.CopyMarketLeaderDetailConstants.KEY_TOTAL_SUB, Sort.by("createdAt").descending());
        mlUserDto.setCopyMarketLeaderDetailsDTO(mlDetails);
        return mlUserDto;
    }

    @Override
    public List<User> findAllUserByAuthorityTypeAndStatus(String authName, boolean activated) {
        return userCustomRepository.findAllUserByAuthorityTypeAndStatus(authName, activated);
    }

    @Override
    public List<User> findAllByIdsAndTypeAndStatus(List<Long> mlUserIds, String authority, Boolean status) {
        return userCustomRepository.findAllByIdsAndTypeAndStatus(mlUserIds, authority, status);
    }

    @Override
    public List<User> findAll() {
        return this.userCustomRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long mlUserId) {
        return userCustomRepository.findById(mlUserId);
    }

    @Override
    public GenericResponse<List<MtsMarketLeadersResponse>> findAllMarketLeader(MtsMarketLeadersRequest request,
                                                                               RequestContext<MtsMarketLeadersRequest> ctx) {
        String ctxId = ctx.getId();
        log.info("[findAllUser] ctx: {}, request: {}", ctxId, request);
        GenericResponse<List<MtsMarketLeadersResponse>> response;
        try {
            Pageable pageable = PageRequest.of(request.buildDefaultPageNumber(request.getPageNumber()), request.buildDefaultPageSize(request.getPageSize()));
            Page<User> userPage = this.userCustomRepository.findAllMarketLeader(request.getMlUsername(), AuthoritiesConstants.MARKET_LEADER, Boolean.TRUE, pageable);
            log.info("[findAllUser] ctxId: {}, userPage: {}", ctxId, Util.objectToStringJsonIgnoreError(userPage));
            List<Long> mlUserIds = userPage.map(User::getId).stream().collect(Collectors.toList());
            Map<Long, Long> totalSubs = this.copyMarketLeaderDetailsCustomService
                .findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(
                    mlUserIds, Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING,
                    Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO,
                    Constants.CopyMarketLeaderDetailConstants.KEY_TOTAL_SUB, Sort.by("createdAt").descending())
                .stream().collect(Collectors.toMap(dt -> dt.getMlUserId().getId(),
                    detail -> commonService.convertTotalSubStrToLong(detail.getValue(), ctxId)));
            log.info("[findAllUser] ctxId: {}, totalSubs: {}", ctxId, totalSubs);
            Page<MtsMarketLeadersResponse> marketLeadersPage = userPage.map(mlUser -> MtsMarketLeadersResponse.builder()
                .marketLeaderId(mlUser.getId()).username(mlUser.getLogin()).fullname(mlUser.getFullName())
                .introduction(mlUser.getIntroduction()).totalSubscribers(totalSubs.get(mlUser.getId()))
                .roles(mlUser.getAuthorities())
                .build());
            log.info("[findAllUser] ctxId:{}, pageResponse: {}", ctxId, marketLeadersPage);
            // Response
            response = GenericResponse.success("");
            response.setData(marketLeadersPage.getContent());
            GenericResponse.buildingPageData(response, marketLeadersPage);
        } catch (Exception e) {
            if (Boolean.TRUE.equals(propConf.getIsEnableApiResponseDefault())) {
                throw e;
            }
            log.error("[findAllUser] ctxId: {}, error: {}", ctxId, Util.objectToStringJsonIgnoreError(e.getStackTrace()));
            response = GenericResponse.internalServerError(e.getMessage());
        }
        log.info("[findAllUser] ctxId: {}, response: {}", ctxId, response);
        return response;
    }

    @Override
    public GenericResponse<MarketLeaderProfileResponse> findMarketLeaderProfile(MarketLeaderProfileRequest request,
                                                                                RequestContext<MarketLeaderProfileRequest> ctx) {
        String ctxId = ctx.getId();
        String methodName = "findMarketLeaderProfile";
        String prefixLog = String.format("%s -- ctxId: %s", methodName, ctxId);
        GenericResponse<MarketLeaderProfileResponse> response;
        try {
            log.info("{} request: {}", prefixLog, request);
            // Get cache
            String key = String.format("%s_%s_%s"
                , Constants.CacheNames.EXPIRED_IN15_MINUTES, methodName, request.objToString());
            if (redisDaoExtend.isExists(key)) {
                String cacheData = redisDaoExtend.get(key, String.class);
                response = commonService.getObjectMapper().readValue(cacheData, new TypeReference<>() {
                });
                log.info("{} -- cache response: {}", prefixLog, Util.objectToStringJsonIgnoreError(response));
                return response;
            }

            if (Objects.isNull(request.getMarketLeaderId())) {
                log.info("{} Param marketLeaderId is null", prefixLog);
                throw new GeneralException(Constants.MARKET_LEADER_ID_IS_REQUIRED);
            }
            response = GenericResponse.success("");
            User uInfo = this.userCustomRepository.findById(request.getMarketLeaderId())
                .orElseThrow(() -> new GeneralException(Constants.INVALID_MARKET_LEADER));
            log.info("{} market leader user info: {}", prefixLog, uInfo);
            Long totalSub = this.copyMarketLeaderDetailsCustomService
                .findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(
                    List.of(uInfo.getId()), Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING,
                    Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO,
                    Constants.CopyMarketLeaderDetailConstants.KEY_TOTAL_SUB, Sort.by(Constants.CREATED_AT).descending())
                .stream().findFirst().stream()
                .mapToLong(x -> commonService.convertTotalSubStrToLong(x.getValue(), ctxId)).sum();
            Double pflRatio = this.commonService.getProfitLossRatio(uInfo.getId(), PeriodEnum.MONTH.name(), ctxId);
            MarketLeaderProfileResponse data = MarketLeaderProfileResponse.builder()
                .marketLeaderId(uInfo.getId()).username(uInfo.getLogin()).fullname(uInfo.getFullName())
                .introduction(uInfo.getIntroduction()).imageUrl(uInfo.getImageUrl()).email(uInfo.getEmail())
                .status(uInfo.isActivated() ? Constants.ACTIVE : Constants.INACTIVE)
                .deactivatedBy(uInfo.getDeactivatedBy())
                .invitedBy(uInfo.getInvitedBy())
                .totalSubscribers(totalSub)
                .profitLossRatio(pflRatio)
                .activatedAt(Objects.nonNull(uInfo.getCreatedDate()) ? uInfo.getCreatedDate()
                    .format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)) : null)
                .deactivatedAt(Objects.nonNull(uInfo.getDeactivatedAt()) ? uInfo.getDeactivatedAt()
                    .format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)) : null)
                .build();
            response.setData(data);

            // Set cache
            if (!redisDaoExtend.isExists(key)) {
                redisDaoExtend.set(key, commonService.objectToStringJson(response), (long) propConf.getRedis().getTimeout().getFifteenMilliseconds());
            }
        } catch (GeneralException e) {
            log.error(Util.objectToStringJsonIgnoreError(e.getStackTrace()));
            throw e;
        } catch (Exception e) {
            if (Boolean.TRUE.equals(propConf.getIsEnableApiResponseDefault())) {
                throw new RuntimeException(e);
            }
            log.info("{} error: ", prefixLog, e);
            response = GenericResponse.internalServerError(e.getMessage());
        }
        log.info("{} response: {}", prefixLog, Util.objectToStringJsonIgnoreError(response));
        return response;
    }

    @Override
    public GenericResponse<CurrentPorfolioResponse> findCurrentPortfolio(CurrentPorfolioRequest request,
                                                                         RequestContext<CurrentPorfolioRequest> ctx) {
        String ctxId = ctx.getId();
        log.info("[findCurrentPortfolio] ctx: {}, request: {}", ctxId, request);
        GenericResponse<CurrentPorfolioResponse> response;
        String methodName = "findCurrentPortfolio";
        String prefixLog = String.format("%s -- ctxId: %s", methodName, ctxId);
        try {
            // Get cache
            String key = String.format("%s_%s_%s"
                , Constants.CacheNames.EXPIRED_IN15_MINUTES, methodName, request.objToString());
            if (redisDaoExtend.isExists(key)) {
                String cacheData = redisDaoExtend.get(key, String.class);
                response = commonService.getObjectMapper().readValue(cacheData, new TypeReference<>() {
                });
                log.info("{} -- cache response: {}", prefixLog, Util.objectToStringJsonIgnoreError(response));
                return response;
            }

            if (Objects.isNull(request.getMarketLeaderId())) {
                log.info("{} -- Param marketLeaderId is null", prefixLog);
                throw new GeneralException(Constants.MARKET_LEADER_ID_IS_REQUIRED);
            }
            User user = this.userCustomRepository.findById(request.getMarketLeaderId())
                .orElseThrow(() -> new GeneralException(Constants.INVALID_MARKET_LEADER_ID));
            if (!user.getAuthorities().contains(new Authority(AuthoritiesConstants.MARKET_LEADER))) {
                throw new GeneralException(Constants.INVALID_MARKET_LEADER_ID);
            }
            if (!user.isActivated()) {
                throw new GeneralException(Constants.INACTIVE_MARKET_LEADER_ID);
            }
            Pageable pageable = PageRequest.of(request.getPageNumber() == null ? 0 : request.getPageNumber(),
                request.getPageSize() == null ? 20 : request.getPageSize());
            Page<CopyPortfolioDetails> page = copyPortfolioDetailsRepository.findAllByMlId(request.getMarketLeaderId(),
                pageable);
            CurrentPorfolioResponse data = CurrentPorfolioResponse.builder()
                .uploadedDateTime(Objects.nonNull(page.getContent()) && !page.getContent().isEmpty()
                    ? page.getContent().get(0).getCopyPortfolioId().getCreatedAt()
                    .format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss))
                    : null)
                .currentPortfolio(page.getContent().stream().map(detail -> CurrentPorfolio.builder()
                        .stockCode(detail.getSymbol())
                        .stockWeight(detail.getWeight()).build())
                    .collect(Collectors.toList()))
                .build();
            response = GenericResponse.success("");
            response.setPageData(
                GenericResponse.PageData.builder()
                    .pageSize(page.getSize())
                    .pageNumber(page.getNumber())
                    .totalPages(page.getTotalPages())
                    .totalElements(page.getTotalElements())
                    .build());
            response.setData(data);
            log.info("{} -- response: {}", prefixLog, Util.objectToStringJsonIgnoreError(response));
            // Set cache
            if (!redisDaoExtend.isExists(key)) {
                redisDaoExtend.set(key, commonService.objectToStringJson(response), (long) propConf.getRedis().getTimeout().getFifteenMilliseconds());
            }
        } catch (GeneralException e) {
            throw new GeneralException(e.getMessage());
        } catch (Exception e) {
            if (Boolean.TRUE.equals(propConf.getIsEnableApiResponseDefault())) {
                throw new RuntimeException(e);
            }
            log.error("[findCurrentPortfolio] ctxId: {}, error: {}", ctx.getId(), e.getMessage());
            response = GenericResponse.internalServerError(e.getMessage());
        }
        return response;
    }

    @Override
    public GenericResponse<List<HistoricalPortfolioResponse>> findHistoricalPortfolio(
        HistoricalPortfolioRequest request,
        RequestContext<HistoricalPortfolioRequest> ctx) {
        log.info("[findHistoricalPortfolio] ctx: {}, request: {}", ctx.getId(), request);
        GenericResponse<List<HistoricalPortfolioResponse>> response;
        try {
            if (Objects.isNull(request.getMarketLeaderId())) {
                log.info("[findHistoricalPortfolio] ctxId: {}, Param marketLeaderId is null", ctx.getId());
                throw new GeneralException(Constants.MARKET_LEADER_ID_IS_REQUIRED);
            }
            User uInfo = this.userCustomRepository.findById(request.getMarketLeaderId())
                .orElseThrow(() -> new GeneralException(Constants.INVALID_MARKET_LEADER_ID));
            if (!uInfo.getAuthorities().contains(new Authority(AuthoritiesConstants.MARKET_LEADER))) {
                throw new GeneralException(Constants.INVALID_MARKET_LEADER_ID);
            }
            if (!uInfo.isActivated()) {
                throw new GeneralException(Constants.INACTIVE_MARKET_LEADER_ID);
            }
            response = GenericResponse.success("");
            if (request.getSortAsc() == null) {
                request.setSortAsc(true);
            }
            Pageable pageable = PageRequest.of(request.getPageNumber() == null ? 0 : request.getPageNumber(),
                request.getPageSize() == null ? 20 : request.getPageSize(),
                request.getSortAsc() ? Sort.by(Sort.Direction.ASC, "createdAt")
                    : Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<CopyPortfolioHistory> page = copyPortfolioHistoryRepository.findByMlUserIdId(
                request.getMarketLeaderId(),
                request.getFromDate(), request.getToDate(), pageable);
            log.info("[findHistoricalPortfolio] ctxId: {}, pageResponse: {}", ctx.getId(), page.getContent());
            List<HistoricalPortfolioResponse> data = page.getContent().stream()
                .map(history -> HistoricalPortfolioResponse.builder()
                    .portfolioId(history.getId())
                    .uploadedDateTime(history.getCreatedAt()
                        .format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)))
                    .build())
                .collect(Collectors.toList());
            response.setPageData(
                GenericResponse.PageData.builder()
                    .pageSize(page.getSize()).pageNumber(page.getNumber())
                    .totalPages(page.getTotalPages()).totalElements(page.getTotalElements())
                    .build());
            response.setData(data);
        } catch (GeneralException e) {
            throw new GeneralException(e.getMessage());
        } catch (Exception e) {
            if (Boolean.TRUE.equals(propConf.getIsEnableApiResponseDefault())) {
                throw e;
            }
            log.error("[findHistoricalPortfolio] ctxId: {}, error: {}", ctx.getId(), e.getMessage());
            response = GenericResponse.internalServerError(e.getMessage());
        }
        return response;
    }

    @Override
    public GenericResponse<HistoricalPortfolioAllStocksResponse> findHistoricalPortfolioAllStocks(
        HistoricalPortfolioAllStocksRequest request, RequestContext<HistoricalPortfolioAllStocksRequest> ctx) {
        log.info("[findHistoricalPortfolioAllStocks] ctx: {}, request: {}", ctx.getId(), request);
        GenericResponse<HistoricalPortfolioAllStocksResponse> response;
        String ctxId = ctx.getId();
        String methodName = "findHistoricalPortfolioAllStocks";
        String prefixLog = String.format("%s -- ctxId: %s", methodName, ctxId);
        try {
            // Get cache
            String key = String.format("%s_%s_%s"
                , Constants.CacheNames.EXPIRED_IN1_DAY, methodName, request.objToString());
            if (redisDaoExtend.isExists(key)) {
                String cacheData = redisDaoExtend.get(key, String.class);
                response = commonService.getObjectMapper().readValue(cacheData, new TypeReference<>() {
                });
                log.info("{} -- cache response: {}", prefixLog, Util.objectToStringJsonIgnoreError(response));
                return response;
            }

            if (Objects.isNull(request.getPortfolioId())) {
                log.info("{} -- Param portfolioId is null", ctx.getId());
                throw new GeneralException(Constants.PORTFOLIO_ID_IS_REQUIRED);
            }
            CopyPortfolioHistory copyPortfolioHistory = this.copyPortfolioHistoryRepository
                .findById(request.getPortfolioId())
                .orElseThrow(() -> new GeneralException(Constants.INVALID_PORTFOLIO_ID));
            User uInfo = copyPortfolioHistory.getMlUserId();
            if (uInfo != null && !uInfo.isActivated()) {
                throw new GeneralException(Constants.CAN_NOT_VIEW_PORTFOLIO_OF_INACTIVE_MARKET_LEADER);
            }
            response = GenericResponse.success("");
            Pageable pageable = PageRequest.of(request.getPageNumber() == null ? 0 : request.getPageNumber(),
                request.getPageSize() == null ? 20 : request.getPageSize());
            Page<CopyPortfolioDetailHistory> page = copyPortfolioDetailsHistoryRepository
                .findByCopyPortfolioIdId(request.getPortfolioId(), pageable);
            log.info("{} -- pageResponse: {}", prefixLog, Util.objectToStringJsonIgnoreError(page));
            HistoricalPortfolioAllStocksResponse data = HistoricalPortfolioAllStocksResponse.builder()
                .marketLeaderId(copyPortfolioHistory.getMlUserId().getId())
                .portfolioId(copyPortfolioHistory.getId())
                .allStockCodes(page.getContent().stream()
                    .map(detail -> HistoricalPortfolioAllStocksResponse.AllStockCode.builder()
                        .stockCode(detail.getSymbol())
                        .stockWeight(detail.getWeight()).build())
                    .collect(Collectors.toList()))
                .build();
            response.setPageData(
                GenericResponse.PageData.builder()
                    .pageSize(page.getSize()).pageNumber(page.getNumber())
                    .totalPages(page.getTotalPages()).totalElements(page.getTotalElements())
                    .build());
            response.setData(data);
            // Set cache
            log.info("{} -- response: {}", prefixLog, Util.objectToStringJsonIgnoreError(response));
            if (!redisDaoExtend.isExists(key)) {
                redisDaoExtend.set(key, commonService.objectToStringJson(response), (long) propConf.getRedis().getTimeout().getOneDayMilliseconds());
            }
        } catch (GeneralException e) {
            throw new GeneralException(e.getMessage());
        } catch (Exception e) {
            if (Boolean.TRUE.equals(propConf.getIsEnableApiResponseDefault())) {
                throw new RuntimeException(e);
            }
            log.error("[findHistoricalPortfolioAllStocks] ctxId: {}, error: {}", ctx.getId(), e.getMessage());
            response = GenericResponse.internalServerError(e.getMessage());
        }
        return response;
    }

}
