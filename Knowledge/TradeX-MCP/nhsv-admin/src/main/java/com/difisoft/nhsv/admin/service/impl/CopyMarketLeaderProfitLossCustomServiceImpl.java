package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.model.v2.db.SymbolDaily;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.nhsv.admin.config.AppConf;
import com.difisoft.nhsv.admin.config.ApplicationProperties;
import com.difisoft.nhsv.admin.config.Messages;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.*;
import com.difisoft.nhsv.admin.domain.enumeration.PeriodEnum;
import com.difisoft.nhsv.admin.domain.enumeration.ProfitLossPeriodEnum;
import com.difisoft.nhsv.admin.domain.request.GetAllMarketLeaderRequest;
import com.difisoft.nhsv.admin.domain.request.MarketLeaderPeriodProfitLossRequest;
import com.difisoft.nhsv.admin.domain.request.MarketLeaderProfitLossRequest;
import com.difisoft.nhsv.admin.domain.request.RecalculateProfitLossByPeriodRequest;
import com.difisoft.nhsv.admin.domain.response.GenericResponse;
import com.difisoft.nhsv.admin.domain.response.GetAllMarketLeaderResponse;
import com.difisoft.nhsv.admin.domain.response.MarketLeaderPeriodProfitLossResponse;
import com.difisoft.nhsv.admin.domain.response.MarketLeaderProfitLossResponse;
import com.difisoft.nhsv.admin.market.StockState;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderProfitLossCustomRepository;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderProfitLossDetailsCustomRepository;
import com.difisoft.nhsv.admin.repository.mongodb.CopySymbolDailyRepository;
import com.difisoft.nhsv.admin.security.AuthoritiesConstants;
import com.difisoft.nhsv.admin.security.SecurityUtils;
import com.difisoft.nhsv.admin.service.*;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
import com.difisoft.nhsv.admin.utils.DateTimeUtil;
import com.difisoft.nhsv.admin.utils.MathUtil;
import com.difisoft.nhsv.admin.utils.TradingDate;
import com.difisoft.nhsv.admin.utils.Util;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scala.App;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Primary
@Slf4j
@Transactional
public class CopyMarketLeaderProfitLossCustomServiceImpl implements CopyMarketLeaderProfitLossCustomService {

    private final CopyMarketLeaderProfitLossCustomRepository copyMarketLeaderProfitLossCustomRepository;
    private final CopyMarketLeaderProfitLossDetailsCustomRepository copyMarketLeaderProfitLossDetailsCustomRepository;
    private final CopyUserService copyUserService;
    private final CopyPortfolioDetailsCustomService copyPortfolioDetailsCustomService;
    private final CopyPortfolioCustomService copyPortfolioCustomService;
    private final StockState stockState;
    private final CommonService commonService;
    private final CopySymbolDailyRepository copySymbolDailyRepository;
    private final CopyMarketLeaderDetailsCustomService copyMarketLeaderDetailsCustomService;
    private final ApplicationProperties appConf;
    private final RedisDaoExtend redisDaoExtend;
    private final AppConf propConf;

    @Autowired
    public CopyMarketLeaderProfitLossCustomServiceImpl(
        CopyMarketLeaderProfitLossCustomRepository copyMarketLeaderProfitLossCustomRepository,
        CopyMarketLeaderProfitLossDetailsCustomRepository copyMarketLeaderProfitLossDetailsCustomRepository,
        CopyUserService copyUserService, CopyPortfolioDetailsCustomService copyPortfolioDetailsCustomService,
        CopyPortfolioCustomService copyPortfolioCustomService, StockState stockState, CommonService commonService,
        CopySymbolDailyRepository copySymbolDailyRepository,
        CopyMarketLeaderDetailsCustomService copyMarketLeaderDetailsCustomService,
        ApplicationProperties appConf,
        RedisDaoExtend redisDaoExtend,
        AppConf propConf) {
        this.copyMarketLeaderProfitLossCustomRepository = copyMarketLeaderProfitLossCustomRepository;
        this.copyMarketLeaderProfitLossDetailsCustomRepository = copyMarketLeaderProfitLossDetailsCustomRepository;
        this.copyUserService = copyUserService;
        this.copyPortfolioDetailsCustomService = copyPortfolioDetailsCustomService;
        this.copyPortfolioCustomService = copyPortfolioCustomService;
        this.stockState = stockState;
        this.commonService = commonService;
        this.copySymbolDailyRepository = copySymbolDailyRepository;
        this.copyMarketLeaderDetailsCustomService = copyMarketLeaderDetailsCustomService;
        this.appConf = appConf;
        this.redisDaoExtend = redisDaoExtend;
        this.propConf = propConf;
    }

    @Override
    public GenericResponse<List<MarketLeaderProfitLossResponse>> findAllMarketLeaderDailyProfitLoss(
        MarketLeaderProfitLossRequest request
        , RequestContext<MarketLeaderProfitLossRequest> ctx
    ) {
        String ctxId = ctx.getId();
        String methodName = "findAllMarketLeaderDailyProfitLoss";
        String prefixLog = String.format("%s -- ctxId: %s", methodName, ctxId);
        log.info("{}, request: {}", prefixLog, request);
        GenericResponse<List<MarketLeaderProfitLossResponse>> response;
        try {

            response = GenericResponse.success("");
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

            if (StringUtils.isBlank(request.getMarketLeaderId())) {
                throw new GeneralException(Constants.MARKET_LEADER_ID_IS_REQUIRED);
            }

            if (StringUtils.isBlank(request.getMarketLeaderId())) {
                throw new GeneralException(Constants.FIELD_IS_REQUIRED);
            }
            String[] mlUserIdsStr = request.getMarketLeaderId().split(",");
            List<Long> mlUserIds = new ArrayList<>();
            for (String str : mlUserIdsStr) {
                try {
                    String id = StringUtils.trim(str);
                    if (StringUtils.isBlank(id)) {
                        continue;
                    }
                    mlUserIds.add(Long.parseLong(id));
                } catch (NumberFormatException e) {
                    throw new NumberFormatException(Constants.INVALID_TYPE);
                }
            }

            // Validate
            ZonedDateTime fromDate = DateTimeUtil.toStartOfDay(
                DateTimeUtil.stringToZoneDateTime(request.getFromDate(), Constants.DATE_FORMAT_yyyyMMdd,
                    Constants.DateTimeType.DATE));
            ZonedDateTime toDate = DateTimeUtil.toEndOfDay(
                DateTimeUtil.stringToZoneDateTime(request.getToDate(), Constants.DATE_FORMAT_yyyyMMdd,
                    Constants.DateTimeType.DATE));
            if (Objects.nonNull(fromDate) && fromDate.toLocalDate().isAfter(LocalDate.now())) {
                log.info("{}, From date must be before today. Input value is: {}", prefixLog, request.getFromDate());
                response = GenericResponse.badRequest(
                    MessageFormat.format(Constants.FROM_DATE_MUST_BE_BEFORE_TODAY, request.getFromDate()));
                return response;
            }

            boolean isSortAsc = request.buildDefaultSortAsc(request.getSortAsc());
            boolean isNotInputDate = Objects.isNull(request.getFromDate()) && Objects.isNull(request.getToDate());
            Pageable pageable = PageRequest.of(
                request.buildDefaultPageNumber(request.getPageNumber()),
                isNotInputDate ? Constants.PAGE_SIZE_30 : Constants.PAGE_SIZE_MAX, isNotInputDate
                    ? Sort.by(Constants.REPORT_DATE_FIELD).descending()
                    : isSortAsc ? Sort.by(Constants.REPORT_DATE_FIELD).ascending()
                    : Sort.by(Constants.REPORT_DATE_FIELD).descending());

            List<MarketLeaderProfitLossResponse> mLProfitLossResponseList = new ArrayList<>();
            for (Long mlUserId : mlUserIds) {
                ZonedDateTime beMarketLeaderDate = commonService.getBeMarketLeaderDateByMlId(mlUserId);
                ZonedDateTime fromDateFinal = Objects.isNull(fromDate) || fromDate.isBefore(beMarketLeaderDate) ? beMarketLeaderDate
                    : fromDate;

                // Get daily profit loss
                MarketLeaderProfitLossResponse data = new MarketLeaderProfitLossResponse();
                String periodName = PeriodEnum.DAY.name();
                Page<CopyMarketLeaderProfitLoss> profitLossesPage = copyMarketLeaderProfitLossCustomRepository
                    .findByMlUserIdAndReportDate(mlUserId, fromDateFinal, toDate, periodName, pageable);
                List<CopyMarketLeaderProfitLoss> profitLossListContent = new ArrayList<>(profitLossesPage.getContent());
                if (CollectionUtils.isEmpty(profitLossListContent)) {
                    log.info("{}, profit loss info of mlUserId = {} is empty ", prefixLog, mlUserId);
                    data.setItems(new ArrayList<>());
                } else {
                    // No need to check firstDate = beMarketLeaderDate because first nav of ML
                    // always = MARKET_LEADER_NAV_FIRST_FIXED
                    double firstNav = isSortAsc ? profitLossListContent.get(0).getNetAssetsValue()
                        : profitLossListContent.get(profitLossListContent.size() - 1).getNetAssetsValue();
                    log.info("{}, mlUserId: {}, firstNav: {}", prefixLog, mlUserId, firstNav);
                    if (isNotInputDate) {
                        Collections.reverse(profitLossListContent);
                    }
                    List<MarketLeaderProfitLossResponse.MarketLeaderDailyProfitLossItem> profitLossItems = profitLossListContent
                        .stream().map(targetItem -> {
                            MarketLeaderProfitLossResponse.MarketLeaderDailyProfitLossItem item = new MarketLeaderProfitLossResponse.MarketLeaderDailyProfitLossItem();
                            item.setReportDate(
                                DateTimeUtil.zonedDateTimeToString(targetItem.getReportDate(), Constants.DATE_FORMAT_PATTERN_1)
                            );
                            item.setProfitLossRatio(targetItem.getProfitLossRatio());
                            item.setNormalisedNAV(
                                ((((BigDecimal.valueOf(targetItem.getNetAssetsValue()).divide(
                                    BigDecimal.valueOf(firstNav), Constants.DEFAULT_SCALE,
                                    RoundingMode.HALF_UP)).subtract(BigDecimal.ONE))
                                    .multiply(BigDecimal.valueOf(100))).setScale(2, RoundingMode.HALF_UP))
                                    .doubleValue());
                            return item;
                        }).collect(Collectors.toList());
                    data.setItems(profitLossItems);
                }
                data.setBeMarketLeaderDate(
                    DateTimeUtil.zonedDateTimeToString(beMarketLeaderDate, Constants.DATE_FORMAT_PATTERN_1)
                );
                data.setMarketLeaderId(mlUserId);
                mLProfitLossResponseList.add(data);
            }

            response.setData(mLProfitLossResponseList);
            log.info("{} -- response: {}", prefixLog, Util.objectToStringJsonIgnoreError(response));

            // Set cache
            if (!redisDaoExtend.isExists(key)) {
                redisDaoExtend.set(key, commonService.objectToStringJson(response));
            }
        } catch (GeneralException e) {
            log.error("error: ", e);
            throw e;
        } catch (Exception e) {
            if (Boolean.TRUE.equals(propConf.getIsEnableApiResponseDefault())) {
                throw new RuntimeException(e);
            }
            log.error("{}, error: ", prefixLog, e);
            response = GenericResponse.internalServerError(e.getMessage());
        }
        log.info("{}, response: {}", prefixLog, response);
        return response;
    }

    @Override
    public GenericResponse<String> recalculateProfitLossByPeriod(
        RecalculateProfitLossByPeriodRequest request, RequestContext<RecalculateProfitLossByPeriodRequest> ctx) {
        String ctxId = ctx.getId();
        try {
            log.info("[recalculateProfitLossByPeriod] ctxId: {}, request: {}", ctxId,
                Util.objectToStringJsonIgnoreError(request));
            // VALIDATE
            List<String> authorities = SecurityUtils.getAuthorities().collect(Collectors.toList());
            if (authorities.stream().noneMatch(
                x -> x.equals(AuthoritiesConstants.ADMIN)
                    || x.equals(AuthoritiesConstants.SUPER_ADMIN))) {
                log.info(
                    "[recalculateProfitLossByPeriod] authority: {}",
                    Util.objectToStringJsonIgnoreError(String.join(",", authorities)));
                throw new GeneralException(Messages.ACCESS_DENIED);
            }

            if (CollectionUtils.isEmpty(request.getMarketLeaderIds())) {
                request.getMarketLeaderIds()
                    .addAll(this.copyUserService
                        .findAllUserByAuthorityTypeAndStatus(AuthoritiesConstants.MARKET_LEADER, Boolean.TRUE)
                        .stream().map(User::getId).collect(Collectors.toList()));
                log.info("[recalculateProfitLossByPeriod] ctxId: {}, userIds: {}", ctxId, request.getMarketLeaderIds());
            }

            ZonedDateTime startDate = Optional.ofNullable(
                    DateTimeUtil.stringToZoneDateTime(request.getStartDate(), Constants.DATE_FORMAT_yyyyMMdd,
                        Constants.DateTimeType.DATE))
                .orElseThrow(() -> new GeneralException(Constants.START_DATE_IS_REQUIRED));
            ZonedDateTime endDate = ZonedDateTime.now();

            // CLEAR ALL profit loss : start --> end
            List<CopyMarketLeaderProfitLoss> profitLossRemovedList = this.copyMarketLeaderProfitLossCustomRepository
                .findByMlUserIdsAndReportDatePeriod(request.getMarketLeaderIds(), startDate, endDate);
            log.info("[recalculateProfitLossByPeriod] ctxId: {}, profitLossRemovedList size: {}, items: {}", ctxId,
                profitLossRemovedList.size(), Util.objectToStringJsonIgnoreError(profitLossRemovedList));
            List<CopyMarketLeaderProfitLossDetails> profitLossDetailsList = this.copyMarketLeaderProfitLossDetailsCustomRepository
                .findAllByCopyMarketLeaderProfitLossIds(
                    profitLossRemovedList.parallelStream()
                        .map(CopyMarketLeaderProfitLoss::getId).collect(Collectors.toList()));
            log.info("[recalculateProfitLossByPeriod] ctxId: {}, profitLossDetailsList size: {}, items: {}", ctxId,
                profitLossDetailsList.size(), Util.objectToStringJsonIgnoreError(profitLossDetailsList));
            this.copyMarketLeaderProfitLossCustomRepository.deleteAll(profitLossRemovedList);
            this.copyMarketLeaderProfitLossDetailsCustomRepository.deleteAll(profitLossDetailsList);

            // RECALCULATE profit loss
            List<LocalDate> dateReportList = startDate.toLocalDate().datesUntil(endDate.toLocalDate().plusDays(1))
                .filter(DateTimeUtil::isWorkingDate).collect(Collectors.toList());
            List<CopyPortfolio> copyPortfolios = this.copyPortfolioCustomService
                .findAllByMLUserIdsHasPortfolioDetailsInfo(request.getMarketLeaderIds());
            log.info("[recalculateProfitLossByPeriod] ctxId: {}, copyPortfolios: {}", ctxId, copyPortfolios);
            dateReportList.forEach(reportDate -> calcProfitLoss(copyPortfolios,
                reportDate.atStartOfDay(ZoneId.systemDefault()), ctxId));
            return GenericResponse.success(HttpStatus.OK.getReasonPhrase());
        } catch (Exception e) {
            log.error("[recalculateProfitLossByPeriod] ctxId: {}, error: ", ctxId, e);
            return GenericResponse.internalServerError(e.getMessage());
        }
    }

    @Override
    @Scheduled(cron = "${app.cron.daily-profit-loss}")
    public void dailyProfitLossJob() {
//        if (appConf.isEnableJob()) {
//            log.info("[dailyProfitLossJob] Job is disabled");
//            return;
//        }
        try {
            ZonedDateTime currentDate = ZonedDateTime.now();
            String ctxId = String.format("dailyProfitLossJob_%s", currentDate);
            log.info("[dailyProfitLossJob] START: {}", currentDate);
            try {
                if (!DateTimeUtil.isWorkingDate(currentDate.toLocalDate())) {
                    log.info("[dailyProfitLossJob] ctxId: {}, Do not run the job on a holiday: {}", ctxId,
                        currentDate.format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyyMMdd)));
                    return;
                }

                // Clear cache
                redisDaoExtend
                    .keys(propConf.getRedis().getKeyPattern().getCacheDailyProfitLoss())
                    .forEach(redisDaoExtend::deleteAKey);

                // REMOVE profit loss invalid: before be market leader date || associated with inactive_user or non-ML
                // inactive_user or non-ML
                this.removeAllProfitLossInValid(ctxId);

                // GET ALL market leader
                List<User> mlUsers = this.copyUserService
                    .findAllUserByAuthorityTypeAndStatus(AuthoritiesConstants.MARKET_LEADER, Boolean.TRUE);
                log.info("[dailyProfitLossJob] ctxId: {}, mlUsers: {}", ctxId, mlUsers);

                // CALC profit loss
                List<CopyPortfolio> copyPortfolios = this.copyPortfolioCustomService
                    .findAllByMLUserIdsHasPortfolioDetailsInfo(
                        mlUsers.parallelStream().map(User::getId).collect(Collectors.toList()));
                log.info("[dailyProfitLossJob] ctxId: {}, copyPortfolios: {}", ctxId, copyPortfolios);
                calcProfitLoss(copyPortfolios, currentDate, ctxId);
            } catch (Exception e) {
                log.error("[dailyProfitLossJob] ctxId: {}, error: ", ctxId, e);
            }
            log.info("[dailyProfitLossJob] END: {}", LocalDateTime.now().atZone(ZoneId.systemDefault()));
        } catch (Exception e) {
            log.error("[dailyProfitLossJob] error: ", e);
        }
    }

    public void calcProfitLoss(List<CopyPortfolio> copyPortfolios, ZonedDateTime currentDate, String ctxId) {

        // Remove history same day report
        List<CopyMarketLeaderProfitLoss> profitSameDayHistory = this.copyMarketLeaderProfitLossCustomRepository
            .findAllByMlUserIdAndReportDate(
                copyPortfolios.stream().map(x -> x.getMlUserId().getId()).collect(Collectors.toList()),
                currentDate.toLocalDate().atStartOfDay(ZoneId.systemDefault()),
                currentDate.toLocalDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()));
        List<CopyMarketLeaderProfitLossDetails> profitDetailSameDayHistory = this.copyMarketLeaderProfitLossDetailsCustomRepository
            .findAllByCopyMarketLeaderProfitLossIdAndReportDate(
                profitSameDayHistory.stream().map(CopyMarketLeaderProfitLoss::getId).collect(
                    Collectors.toList()),
                currentDate.toLocalDate().atStartOfDay(ZoneId.systemDefault()),
                currentDate.toLocalDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()));
        this.copyMarketLeaderProfitLossCustomRepository.deleteAll(profitSameDayHistory);
        this.copyMarketLeaderProfitLossDetailsCustomRepository.deleteAll(profitDetailSameDayHistory);

        for (CopyPortfolio portfolio : copyPortfolios) {
            List<CopyMarketLeaderProfitLossDetails> profitLossDetailsList = new ArrayList<>();
            User user = portfolio.getMlUserId();
            Long portfolioId = portfolio.getId();
            Long userId = user.getId();
            String prefixLog = String.format("calcProfitLoss_%s_%s_%s", ctxId, userId, portfolioId);
            List<CopyMarketLeaderProfitLoss> profitLossHistory = this.copyMarketLeaderProfitLossCustomRepository
                .findAllByMlUserIdAndReportDateIsLessThanOrderByReportDateDesc(user, currentDate);
            log.info("[calcProfitLoss] prefixLog: {}, profitLossHistory: {}", prefixLog,
                Util.objectToStringJsonIgnoreError(profitLossHistory));

            for (PeriodEnum period : PeriodEnum.values()) {
                CopyMarketLeaderProfitLoss profitLoss = new CopyMarketLeaderProfitLoss();
                profitLoss.setType(period.name());
                profitLoss.setMlUserId(user);
                profitLoss.setReportDate(currentDate);
                profitLoss.setCreatedAt(currentDate);
                List<CopyPortfolioDetails> portfolioDetails = this.copyPortfolioDetailsCustomService
                    .findAllByCopyPortfolioIds(List.of(portfolio.getId()));
                log.info("[calcProfitLoss] prefixLog: {}, portfolioDetails: {}", prefixLog,
                    Util.objectToStringJsonIgnoreError(portfolioDetails));
                if (CollectionUtils.isEmpty(profitLossHistory)) {
                    // case first report
                    profitLoss.setNetAssetsValue(Constants.MARKET_LEADER_NAV_FIRST_FIXED.doubleValue());
                    profitLoss.setProfitLossRatio(0D);
                    if (Objects.equals(period, PeriodEnum.DAY)) {
                        profitLossDetailsList.addAll(makeProfitLossDetail(portfolioDetails, prefixLog, user,
                            profitLoss.getNetAssetsValue(), currentDate));
                    }
                } else {
                    CopyMarketLeaderProfitLoss latestHistoryReport = profitLossHistory.get(0);
                    log.info("[calcProfitLoss] prefixLog: {}, latestReport: {}", prefixLog, latestHistoryReport);
                    List<CopyMarketLeaderProfitLossDetails> latestProfitLossDetails = this.copyMarketLeaderProfitLossDetailsCustomRepository
                        .findAllByCopyMarketLeaderProfitLossIds(List.of(latestHistoryReport.getId()));
                    log.info("[calcProfitLoss] prefixLog: {}, latestProfitLossDetails: {}", prefixLog,
                        latestProfitLossDetails);
                    BigDecimal navT0 = latestProfitLossDetails.stream().map(profitLossDetail -> {
                        Double lastPrice = getLastPriceByCodeAndDate(profitLossDetail.getStockCode(), currentDate);
                        log.info("[calcProfitLoss] prefixLog: {}, symbol: {}, lastPrice: {}, quantity: {}", prefixLog,
                            profitLossDetail.getStockCode(), lastPrice, profitLossDetail.getStockQuantity());
                        return BigDecimal.valueOf(profitLossDetail.getStockQuantity())
                            .multiply(BigDecimal.valueOf(lastPrice));
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
                    log.info("[calcProfitLoss] prefixLog: {},  navT0: {}", prefixLog, navT0);
                    if (period == PeriodEnum.DAY) {
                        BigDecimal profitLossRatio = ((MathUtil.divideDoubleIgnoreNullOrZero(
                            navT0.doubleValue(), latestHistoryReport.getNetAssetsValue(), Constants.DEFAULT_SCALE,
                            RoundingMode.HALF_UP).subtract(BigDecimal.ONE)).multiply(BigDecimal.valueOf(100)))
                            .setScale(2, RoundingMode.HALF_UP);
                        profitLoss.setNetAssetsValue(navT0.doubleValue());
                        profitLoss.setProfitLossRatio(profitLossRatio.doubleValue());
                        // CALC quantity
                        profitLossDetailsList.addAll(makeProfitLossDetail(portfolioDetails, prefixLog, user,
                            profitLoss.getNetAssetsValue(), currentDate));
                    } else {
                        BigDecimal navT1;
                        if (profitLossHistory.size() == 1) {
                            navT1 = BigDecimal.valueOf(profitLossHistory.get(0).getNetAssetsValue());
                        } else {
                            LocalDate lastDate = calDate(period.name());
                            navT1 = BigDecimal
                                .valueOf(profitLossHistory.stream()
                                    .filter(x -> x.getReportDate().toLocalDate().isAfter(lastDate)
                                        || x.getReportDate().toLocalDate().isEqual(lastDate))
                                    .sorted((o1, o2) -> {
                                        if (o1.getReportDate().isAfter(o2.getReportDate())) {
                                            return 1;
                                        } else if (o1.getReportDate().isBefore(o2.getReportDate())) {
                                            return -1;
                                        } else {
                                            return 0;
                                        }
                                    }).map(CopyMarketLeaderProfitLoss::getNetAssetsValue).findFirst()
                                    .orElse(Constants.MARKET_LEADER_NAV_FIRST_FIXED.doubleValue()));
                        }
                        log.info("[calcProfitLoss] prefixLog: {},  navT1: {}", prefixLog, navT1);
                        BigDecimal profitLossRatio = ((MathUtil.divideDoubleIgnoreNullOrZero(
                                navT0.doubleValue(), navT1.doubleValue(), Constants.DEFAULT_SCALE, RoundingMode.HALF_UP)
                            .subtract(BigDecimal.ONE)).multiply(BigDecimal.valueOf(100)))
                            .setScale(2, RoundingMode.HALF_UP);
                        profitLoss.setNetAssetsValue(null);
                        profitLoss.setProfitLossRatio(profitLossRatio.doubleValue());
                    }
                }

                // Save report
                CopyMarketLeaderProfitLoss profitLossResult = this.copyMarketLeaderProfitLossCustomRepository
                    .save(profitLoss);
                log.info("[calcProfitLoss] prefixLog: {}, profitLossResult: {}", prefixLog, profitLossResult);
                if (period == PeriodEnum.DAY) {
                    profitLossDetailsList.forEach(detail -> detail.setCopyMarketLeaderProfitLossId(profitLossResult));
                    List<CopyMarketLeaderProfitLossDetails> detailResults = this.copyMarketLeaderProfitLossDetailsCustomRepository
                        .saveAll(profitLossDetailsList);
                    log.info("[calcProfitLoss] prefixLog: {}, profitDetailResults: {}", prefixLog, detailResults);
                }
            }
        }
    }

    private LocalDate calDate(String period) {
        switch (period) {
            case Constants.WEEK:
                LocalDate week = TradingDate.minusOneWeek();
                while (!DateTimeUtil.isLastDayOfWeek(week)) {
                    week = week.plusDays(1);
                }
                return TradingDate.adjustDown(week);
            case Constants.MONTH:
                LocalDate month = TradingDate.minusOneMonth();
                while (!DateTimeUtil.isLastDayOfMonth(month)) {
                    month = month.plusDays(1);
                }
                return TradingDate.adjustDown(month);
            case Constants.YEAR:
                LocalDate year = TradingDate.minusOneYear();
                while (!DateTimeUtil.isLastDayOfYear(year)) {
                    year = year.plusDays(1);
                }
                return TradingDate.adjustDown(year);
        }
        return LocalDate.now();
    }

    private Double getLastPriceByCodeAndDate(String stockCode, ZonedDateTime reportDate) {
        Double lastPrice = null;
        LocalDate date = reportDate.toLocalDate();
        LocalDate now = LocalDate.now();

        if (date.equals(now)) {
            SymbolInfo symbolInfo = Optional.ofNullable(stockState.getStockState(stockCode))
                .orElseThrow(() -> new GeneralException(
                    MessageFormat.format(
                        "[getLastPriceByCodeAndDate] Symbol info of code: {0} at date: {1} is empty",
                        stockCode, date)));
            log.info("[getLastPriceByCodeAndDate] symbolInfo: {}", Util.objectToStringJsonIgnoreError(symbolInfo));
            lastPrice = symbolInfo.getLast();
        }

        if (date.isBefore(now)) {
            SymbolDaily symbolDaily = copySymbolDailyRepository
                .findByStockCodeAndDateString(stockCode,
                    date.format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyyMMdd)))
                .orElseThrow(() -> new GeneralException(
                    MessageFormat.format(
                        "[getLastPriceByCodeAndDate] Symbol info of code: {0} at date: {1} is empty",
                        stockCode, date)));
            log.info("[getLastPriceByCodeAndDate] symbolDaily: {}", Util.objectToStringJsonIgnoreError(symbolDaily));
            lastPrice = symbolDaily.getLast();
        }

        return lastPrice;
    }

    @Override
    public GenericResponse<List<MarketLeaderPeriodProfitLossResponse>> findMarketLeaderProfitLossByPeriod(
        MarketLeaderPeriodProfitLossRequest request, RequestContext<MarketLeaderPeriodProfitLossRequest> ctx) {
        String ctxId = ctx.getId();
        log.info("[findMarketLeaderProfitLossByPeriod] ctxId: {}, request: {}, ctx: {}", ctxId, request, ctx);
        GenericResponse<List<MarketLeaderPeriodProfitLossResponse>> response;
        int pageSize = request.buildDefaultPageSize(request.getPageSize());
        int pageNumber = request.buildDefaultPageNumber(request.getPageNumber());
        boolean requestSortAsc = request.buildDefaultSortAsc(request.getSortAsc());
        String requestPeriod = Objects.isNull(request.getPeriod()) ? ProfitLossPeriodEnum.THREE_MONTH.getKey()
            : request.getPeriod();
        List<Long> requestMlUserIds = request.getMarketLeaderIds();
        try {
            // Validate
            if (CollectionUtils.isNotEmpty(requestMlUserIds)) {
                List<Long> mlUserActiveIds = this.copyUserService.findAllByIdsAndTypeAndStatus(
                        requestMlUserIds, AuthoritiesConstants.MARKET_LEADER, Boolean.TRUE).parallelStream()
                    .map(User::getId).collect(Collectors.toList());
                if (!Objects.equals(requestMlUserIds.size(), mlUserActiveIds.size())) {
                    throw new GeneralException(
                        Constants.INACTIVE_MARKET_LEADER_ID,
                        String.format(
                            "The list of IDs is invalid [either inactive market leaders or not market leaders]: %s",
                            requestMlUserIds.parallelStream().filter(x -> !mlUserActiveIds.contains(x))
                                .map(Object::toString).collect(Collectors.joining(","))));
                }
            }
            // GET profit loss in period
            ProfitLossPeriodEnum period = ProfitLossPeriodEnum.findEnumByKey(requestPeriod);
            ZonedDateTime currentDate = ZonedDateTime.now();
            ZonedDateTime startDate = calcProfitLossPeriodStartDate(period, currentDate);
            Map<Long, ZonedDateTime> beMarketLeaderDatesMap = commonService
                .getBeMarketLeaderDateByMlIds(requestMlUserIds);

            List<CopyMarketLeaderProfitLoss> profitLossList = this.copyMarketLeaderProfitLossCustomRepository
                .findByMlUserIdsAndReportDatePeriod(request.getMarketLeaderIds(), startDate, currentDate)
                .stream().filter(profitLoss -> {
                    ZonedDateTime beMarketLeaderDate = beMarketLeaderDatesMap.get(profitLoss.getMlUserId().getId());
                    if (Objects.nonNull(beMarketLeaderDate)) {
                        return profitLoss.getReportDate().isAfter(beMarketLeaderDate)
                            || profitLoss.getReportDate().isEqual(beMarketLeaderDate);
                    }
                    return false;
                }).collect(Collectors.toList());

            Map<User, List<CopyMarketLeaderProfitLoss>> profitLossGroupByMlUser = profitLossList.stream().collect(
                Collectors.groupingBy(CopyMarketLeaderProfitLoss::getMlUserId));
            log.info("[findMarketLeaderProfitLossByPeriod] ctxId: {}, profitLossGroupByMlUser: {}", ctxId,
                profitLossGroupByMlUser);

            List<MarketLeaderPeriodProfitLossResponse> profitLossItems = new ArrayList<>();
            profitLossGroupByMlUser.forEach((user, periodProfitLossList) -> {
                MarketLeaderPeriodProfitLossResponse item = new MarketLeaderPeriodProfitLossResponse();
                AtomicReference<Double> navStartDate = new AtomicReference<>(null);
                AtomicReference<Double> navLatestDate = new AtomicReference<>(null);
                // get profit loss start date and current date
                if (periodProfitLossList.size() == 1) {
                    navLatestDate.set(periodProfitLossList.get(0).getNetAssetsValue());
                    navStartDate.set(Constants.MARKET_LEADER_NAV_FIRST_FIXED.doubleValue());
                } else {
                    // Case list size > 1 ---> value NAV is at boundary of list
                    periodProfitLossList.forEach(x -> {
                        navLatestDate.set(periodProfitLossList.get(0).getNetAssetsValue());
                        navStartDate.set(periodProfitLossList.get(periodProfitLossList.size() - 1).getNetAssetsValue());
                    });
                }
                item.setMarketLeaderId(user.getId());
                item.setCurrentDate(currentDate);
                log.info(
                    "[findMarketLeaderProfitLossByPeriod] ctxId: {}, userID: {}, navLatestDate: {}, navStartDate: {}",
                    ctxId, user.getId(), navLatestDate.get(), navStartDate.get());
                item.setProfitLossRatio(((((MathUtil.divideDoubleIgnoreNullOrZero(navLatestDate.get(),
                    navStartDate.get(), Constants.DEFAULT_SCALE, RoundingMode.HALF_UP))
                    .subtract(BigDecimal.ONE)).multiply(BigDecimal.valueOf(100))).setScale(2, RoundingMode.HALF_UP))
                    .doubleValue());
                profitLossItems.add(item);
            });

            // Response
            response = GenericResponse.success("");
            profitLossItems
                .sort((i1, i2) -> requestSortAsc ? (i1.getProfitLossRatio().compareTo(i2.getProfitLossRatio()))
                    : (i2.getProfitLossRatio().compareTo(i1.getProfitLossRatio())));
            log.info("[findMarketLeaderProfitLossByPeriod] ctxId: {}, profitLossItems: {}", ctxId, profitLossItems);
            GenericResponse.customBuildingPageData(response, profitLossItems, pageNumber, pageSize);
        } catch (GeneralException e) {
            log.error("error: ", e);
            throw e;
        } catch (Exception e) {
            if (Boolean.TRUE.equals(propConf.getIsEnableApiResponseDefault())) {
                throw e;
            }
            log.error("[findMarketLeaderProfitLossByPeriod] ctxId: {}, error: ", ctxId, e);
            response = GenericResponse.internalServerError(e.getMessage());
        }
        log.info("[findMarketLeaderProfitLossByPeriod] ctxId: {}, response: {}", ctxId, response);
        return response;
    }

    private void removeAllProfitLossInValid(String ctxId) {
        List<CopyMarketLeaderProfitLoss> profitLossesRemoved = new ArrayList<>();

        List<Long> userIsInactiveOrNonMLIds = this.copyUserService.findAll()
            .parallelStream().filter(
                x -> !x.isActivated()
                    || x.getAuthorities().parallelStream().noneMatch(y -> y.getName()
                    .equals(AuthoritiesConstants.MARKET_LEADER)))
            .map(User::getId).collect(Collectors.toList());
        log.info("[dailyProfitLossJob] ctxId: {}, userIsInactiveOrNonML: {}", ctxId,
            Util.objectToStringJsonIgnoreError(userIsInactiveOrNonMLIds));

        profitLossesRemoved.addAll(this.copyMarketLeaderProfitLossCustomRepository
            .findAllByMlUserIdAndReportDate(userIsInactiveOrNonMLIds, null, null));

        profitLossesRemoved.addAll(this.copyMarketLeaderProfitLossCustomRepository
            .getAllProfitLossBeforeBeMarketLeaderDate(
                AuthoritiesConstants.MARKET_LEADER, Boolean.TRUE,
                Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING,
                Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO,
                Constants.CopyMarketLeaderDetailConstants.KEY_BE_MARKET_LEADER_DATE));
        log.info("[dailyProfitLossJob] ctxId: {}, profitLossesRemoved removed: {}", ctxId,
            Util.objectToStringJsonIgnoreError(
                profitLossesRemoved.parallelStream()
                    .map(x -> String.format("[mlID: %s, plID: %s]", x.getMlUserId().getId(), x.getId()))
                    .collect(Collectors.joining(", "))));
        List<Long> profitLossIdsRemoved = profitLossesRemoved.parallelStream().map(CopyMarketLeaderProfitLoss::getId)
            .collect(Collectors.toList());
        this.copyMarketLeaderProfitLossCustomRepository.deleteAll(profitLossesRemoved);
        this.copyMarketLeaderProfitLossDetailsCustomRepository.deleteAllByProfitLossIds(profitLossIdsRemoved);
    }

    private ZonedDateTime calcProfitLossPeriodStartDate(ProfitLossPeriodEnum period, ZonedDateTime currentDate) {
        ZonedDateTime startDate;
        switch (period) {
            case ONE_DAY:
                startDate = currentDate.minusDays(1);
                break;
            case ONE_WEEK:
                startDate = currentDate.minusWeeks(1);
                break;
            case ONE_MONTH:
                startDate = currentDate.minusMonths(1);
                break;
            case THREE_MONTH:
                startDate = currentDate.minusMonths(3);
                break;
            case ONE_YEAR:
                startDate = currentDate.minusYears(1);
                break;
            default:
                throw new GeneralException(
                    MessageFormat.format(Messages.PROFIT_LOSS_PERIOD_TYPE_ENUM_NOT_FOUND, period));
        }
        return getBeforeWorkingDate(startDate);
    }

    private List<CopyMarketLeaderProfitLossDetails> makeProfitLossDetail(
        List<CopyPortfolioDetails> portfolioDetails, String prefixLog, User user, Double nav,
        ZonedDateTime currentDate) {
        return portfolioDetails.stream().map(pDetail -> {
            Double lastPrice = getLastPriceByCodeAndDate(pDetail.getSymbol(), currentDate);
            log.info("[makeProfitLossDetail] prefixLog: {}, nav: {}, symbol: {}, lastPrice: {}", prefixLog, nav,
                pDetail.getSymbol(), lastPrice);
            CopyMarketLeaderProfitLossDetails detail = new CopyMarketLeaderProfitLossDetails();
            detail.setReportDate(currentDate);
            detail.setStockCode(pDetail.getSymbol());
            detail.setStockQuantity(
                ((BigDecimal.valueOf(nav).multiply(BigDecimal.valueOf(pDetail.getWeight())))
                    .divide(BigDecimal.valueOf(lastPrice), Constants.DEFAULT_SCALE, RoundingMode.HALF_UP))
                    .longValue());
            detail.setMlUserId(user);
            detail.setCreatedAt(currentDate);
            return detail;
        }).collect(Collectors.toList());
    }

    private ZonedDateTime getBeforeWorkingDate(ZonedDateTime date) {
        return DateTimeUtil.isWorkingDate(date.toLocalDate()) ? date
            : TradingDate.minusOne(date.toLocalDate()).atStartOfDay(ZoneId.systemDefault());
    }

    public GenericResponse<List<GetAllMarketLeaderResponse>> getAllMarketLeader(GetAllMarketLeaderRequest request,
                                                                                RequestContext<GetAllMarketLeaderRequest> ctx) {
        String txid = ctx.getId();
        log.info("[getAllMarketLeader] txid: {}, request: {}", txid, request);
        GenericResponse<List<GetAllMarketLeaderResponse>> response;
        List<User> marketLeaders = this.copyUserService
            .findAllUserByAuthorityTypeAndStatus(AuthoritiesConstants.MARKET_LEADER, Boolean.TRUE);
        if (request.getSearch() != null) {
            marketLeaders = marketLeaders.stream()
                .filter(x -> x.getLogin().toLowerCase().contains(request.getSearch().toLowerCase())
                    || x.getFullName().toLowerCase().contains(request.getSearch().toLowerCase()))
                .collect(Collectors.toList());
        }
        String period = "MONTH";
        if (request.getPeriod() != null && !request.getPeriod().isEmpty()) {
            try {
                period = PeriodEnum.valueOf(request.getPeriod()).name();
            } catch (IllegalArgumentException e) {
                period = "MONTH";
                request.setPeriod("ALL");
            }
        }

        Map<Long, ZonedDateTime> latestCreatedMap = marketLeaders.stream()
            .map(user -> copyPortfolioCustomService.findCreatedAtByUserId(user.getId()))
            .filter(Optional::isPresent)
            .collect(Collectors.toMap(
                o -> o.get().getMlUserId().getId(),
                o -> o.get().getCreatedAt(),
                (existing, replacement) -> existing));


        Map<Long, ZonedDateTime> firstCreatedMap = marketLeaders.stream()
            .collect(Collectors.toMap(User::getId, user -> copyUserService.findById(user.getId()).get().getCreatedDate()));

        List<GetAllMarketLeaderResponse> profitLossItems = new ArrayList<>();
        for (User marketLeader : marketLeaders) {
            GetAllMarketLeaderResponse item = new GetAllMarketLeaderResponse();
            item.setMarketLeaderId(marketLeader.getId());
            item.setUsername(marketLeader.getLogin());
            item.setFullname(marketLeader.getFullName());
            item.setImageUrl(marketLeader.getImageUrl());
            item.setProfitLossRatio(commonService.getProfitLossRatio(marketLeader.getId(), period, txid));
            item.setTotalSubscribers((commonService.convertTotalSubStrToLong(this.getTotalSubscribers(
                marketLeader.getId(), ctx.getId()), ctx.getId())));
            profitLossItems.add(item);
        }
        response = GenericResponse.success("");
        profitLossItems = profitLossItems.stream().sorted((o1, o2) -> {
            ZonedDateTime latestCreated1 = latestCreatedMap.get(o1.getMarketLeaderId());
            ZonedDateTime latestCreated2 = latestCreatedMap.get(o2.getMarketLeaderId());
            ZonedDateTime firstCreated1 = firstCreatedMap.get(o1.getMarketLeaderId());
            ZonedDateTime firstCreated2 = firstCreatedMap.get(o2.getMarketLeaderId());
            switch (request.getCategory()) {
                case "POPULARITY":
                    return o2.getTotalSubscribers().compareTo(o1.getTotalSubscribers());
                case "PORTFOLIO_UPDATE":
                    return compareZonedDateTime(latestCreated1, latestCreated2, true);
                case "ACCOUNT_REGISTRATION":
                    return firstCreated1.compareTo(firstCreated2);
                case "PROFIT_RATE":
                default:
                    if (request.getPeriod().equals("ALL")) {
                        return o1.getFullname().compareTo(o2.getFullname());
                    } else {
                        if (o1.getProfitLossRatio() > o2.getProfitLossRatio()) {
                            return -1;
                        } else if (o1.getProfitLossRatio() < o2.getProfitLossRatio()) {
                            return 1;
                        } else {
                            return o1.getFullname().compareTo(o2.getFullname());
                        }
                    }
            }
        }).collect(Collectors.toList());
        GenericResponse.customBuildingPageData(response, profitLossItems, request.getPageNumber(),
            request.getPageSize());
        return response;
    }

    private String getTotalSubscribers(Long id, String txid) {
        log.info("[getTotalSubscribers] txid: {}, id: {}", txid, id);
        List<CopyMarketLeaderDetailsDTO> mlDetails = copyMarketLeaderDetailsCustomService
            .findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(
                List.of(id), Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING,
                Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO,
                Constants.CopyMarketLeaderDetailConstants.KEY_TOTAL_SUB, Sort.by("createdAt").descending());
        if (CollectionUtils.isEmpty(mlDetails)) {
            return "0";
        } else {
            return mlDetails.get(0).getValue();
        }
    }
    private int compareZonedDateTime(ZonedDateTime dt1, ZonedDateTime dt2, boolean reverseOrder) {
        if (dt1 == null && dt2 == null) return 0;
        if (dt1 == null) return reverseOrder ? 1 : -1;
        if (dt2 == null) return reverseOrder ? -1 : 1;
        return reverseOrder ? dt2.compareTo(dt1) : dt1.compareTo(dt2);
    }
}
