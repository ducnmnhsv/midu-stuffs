package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.responses.Response;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.CopyPortfolio;
import com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory;
import com.difisoft.nhsv.admin.domain.CopyPortfolioDetails;
import com.difisoft.nhsv.admin.domain.CopyPortfolioHistory;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.domain.request.CopyTradingRequest;
import com.difisoft.nhsv.admin.domain.request.PortfolioUploadRequest;
import com.difisoft.nhsv.admin.market.StockState;
import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailsCustomRepository;
import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailsRepository;
import com.difisoft.nhsv.admin.service.CommonService;
import com.difisoft.nhsv.admin.service.CopyPortfolioCustomService;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailHistoryCustomService;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailsCustomService;
import com.difisoft.nhsv.admin.service.CopyPortfolioHistoryCustomService;
import com.difisoft.nhsv.admin.service.CopyUserService;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailsDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioDetailsMapper;
import com.difisoft.nhsv.admin.utils.Util;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolioDetails}.
 */
@Service("copyPortfolioDetailsCustomService")
@Transactional
@Slf4j
@Primary
public class CopyPortfolioDetailsCustomServiceImpl extends CopyPortfolioDetailsServiceImpl implements CopyPortfolioDetailsCustomService {

    @Value("${app.kafka.internal.copy-trading-engine.topic}")
    private String copyEngineTopic;
    @Value("${app.kafka.internal.copy-trading-engine.uri.trigger-portfolio-changed}")
    private String copyEngineTriggerPortfolioChangedUri;
    private final CopyPortfolioDetailsCustomRepository copyPortfolioDetailsCustomRepository;
    private final CopyPortfolioCustomService copyPortfolioCustomService;
    private final CopyPortfolioHistoryCustomService copyPortfolioHistoryCustomService;
    private final CopyPortfolioDetailHistoryCustomService copyPortfolioDetailHistoryCustomService;
    private final CopyUserService copyUserService;
    private final CopyPortfolioDetailsMapper copyPortfolioDetailsMapper;
    private final StockState stockState;
    private final CommonService commonService;

    @Autowired
    public CopyPortfolioDetailsCustomServiceImpl(
        CopyPortfolioDetailsRepository copyPortfolioDetailsRepository
        , CopyPortfolioDetailsMapper copyPortfolioDetailsMapper
        , CopyPortfolioDetailsCustomRepository copyPortfolioDetailsCustomRepository
        , CopyPortfolioCustomService copyPortfolioCustomService
        , CopyPortfolioHistoryCustomService copyPortfolioHistoryCustomService
        , CopyPortfolioDetailHistoryCustomService copyPortfolioDetailHistoryCustomService
        , CopyUserService copyUserService
        , StockState stockState
        , CommonService commonService
    ) {
        super(copyPortfolioDetailsRepository, copyPortfolioDetailsMapper);
        this.copyPortfolioDetailsCustomRepository = copyPortfolioDetailsCustomRepository;
        this.copyPortfolioCustomService = copyPortfolioCustomService;
        this.copyPortfolioHistoryCustomService = copyPortfolioHistoryCustomService;
        this.copyPortfolioDetailHistoryCustomService = copyPortfolioDetailHistoryCustomService;
        this.copyUserService = copyUserService;
        this.copyPortfolioDetailsMapper = copyPortfolioDetailsMapper;
        this.stockState = stockState;
        this.commonService = commonService;
    }

    @Override
    public Page<CopyPortfolioDetailsDTO> findAllByMlId(Long mlID, Pageable pageable) {
        Page<CopyPortfolioDetails> copyPortfolioDetails = copyPortfolioDetailsCustomRepository.findAllByMlId(mlID, pageable);
        log.info("[findAllByMlId] copyMarketLeaderDetails: {}", copyPortfolioDetails);
        return copyPortfolioDetails.map(copyPortfolioDetailsMapper::toDto);
    }

    @Override
    public List<CopyPortfolioDetailsDTO> findAllDTOByCopyPortfolioIds(List<Long> copyPortfolioIds) {
        return this.copyPortfolioDetailsMapper.toDto(this.copyPortfolioDetailsCustomRepository.findAllByCopyPortfolioIds(copyPortfolioIds));
    }

    @Override
    public List<CopyPortfolioDetails> findAllByCopyPortfolioIds(List<Long> copyPortfolioIds) {
        return this.copyPortfolioDetailsCustomRepository.findAllByCopyPortfolioIds(copyPortfolioIds);
    }

    @Override
    public void uploadPortfolio(PortfolioUploadRequest request) {
        log.info("[uploadPortfolio] request: {}", request);
        String errorMsg;
        Long mlUserId = request.getMlUserId();

        if (CollectionUtils.isEmpty(request.getItems())) {
            throw new GeneralException(Constants.PORTFOLIO_SYMBOL_UPLOAD_ITEMS_IS_REQUIRED);
        }

        List<PortfolioUploadRequest.PortfolioUploadItem> symbolItems = request.getItems().stream()
            .peek(x -> {
                String symbol = StringUtils.trim(x.getSymbol());
                x.setSymbol(StringUtils.isBlank(symbol) ? Strings.EMPTY : symbol.toUpperCase());
            }).collect(Collectors.toList());
        log.info("[uploadPortfolio] symbolItems: {}", symbolItems);

        if (symbolItems.stream().anyMatch(x -> StringUtils.isBlank(x.getSymbol()))) {
            throw new GeneralException(Constants.UPLOAD_PORTFOLIO_STOCK_CODE_MUST_NOT_BE_EMPTY);
        }

        Map<String, Long> symbolMap = symbolItems.stream()
            .collect(Collectors.groupingBy(PortfolioUploadRequest.PortfolioUploadItem::getSymbol, Collectors.counting()));
        Optional<String> symbolDuplicate = symbolMap.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Map.Entry::getKey).findFirst();
        if (symbolDuplicate.isPresent()) {
            errorMsg = MessageFormat.format(Constants.UPLOAD_PORTFOLIO_STOCK_CODE_IS_DUPLICATED, symbolDuplicate.get());
            log.info("[uploadPortfolio] " + errorMsg);
            throw new GeneralException(errorMsg);
        }

        // Validate Symbol on Market
        List<String> symbols = stockState.getAllSymbols().stream().map(SymbolInfo::getCode).collect(Collectors.toList());
        List<String> symbolsInvalid = symbolItems.stream()
            .map(PortfolioUploadRequest.PortfolioUploadItem::getSymbol)
            .filter(symbol -> !symbols.contains(symbol))
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(symbolsInvalid)) {
            errorMsg = MessageFormat.format(Constants.UPLOAD_PORTFOLIO_INVALID_STOCK_CODE, symbolsInvalid);
            log.info("[uploadPortfolio] " + errorMsg);
            throw new GeneralException(errorMsg);
        }

        Optional<User> mlUserOpt = copyUserService.findById(mlUserId);
        if (mlUserOpt.isEmpty()) {
            throw new GeneralException(MessageFormat.format("Market leader is not found. Input mlUserId = {0}", mlUserId));
        }
        User mlUser = mlUserOpt.get();
        log.info("[uploadPortfolio] mlUser: {}", mlUser);

        // save portfolio history
        Optional<CopyPortfolio> copyPortfolioOpt = copyPortfolioCustomService.findByMLUserId(mlUserId);
        log.info("[uploadPortfolio] copyPortfolio: {}", copyPortfolioOpt);
        if (copyPortfolioOpt.isPresent()) {
            CopyPortfolio copyPortfolio = copyPortfolioOpt.get();
            log.info("[uploadPortfolio] copyPortfolio: {}", copyPortfolio);
            List<CopyPortfolioDetails> deleteDetails = copyPortfolioDetailsCustomRepository.findAllByCopyPortfolioId(copyPortfolio.getId());
            log.info("[uploadPortfolio] pOldDetails: {}", deleteDetails);
            List<CopyPortfolioDetailHistory> pDetailHistories = deleteDetails.stream().map(item -> {
                CopyPortfolioDetailHistory entity = new CopyPortfolioDetailHistory();
                entity.setWeight(item.getWeight());
                entity.setSymbol(item.getSymbol().trim().toUpperCase());
                entity.setId(item.getId());
                entity.setCreatedAt(item.getCreatedAt());
                return entity;
            }).collect(Collectors.toList());

            CopyPortfolioHistory pHistory = CopyPortfolioHistory.builder()
                .id(copyPortfolio.getId())
                .mlUserId(copyPortfolio.getMlUserId())
                .createdAt(ZonedDateTime.now())
                .build();
            log.info("[uploadPortfolio] pHistory : {}", pHistory);
            copyPortfolioHistoryCustomService.save(pHistory);
            pDetailHistories.forEach(x -> x.setCopyPortfolioHistoryId(pHistory));
            copyPortfolioDetailHistoryCustomService.saveAll(pDetailHistories);

            // Delete old portfolio
            copyPortfolioDetailsCustomRepository.deleteAll(deleteDetails);
            copyPortfolioCustomService.delete(copyPortfolio.getId());
        }

        // save new portfolio
        List<CopyPortfolioDetails> newDetails = symbolItems.stream().map(item -> {
            CopyPortfolioDetails entity = new CopyPortfolioDetails();
            entity.setWeight(item.getWeight() / 100);
            entity.setSymbol(item.getSymbol());
            entity.setCreatedAt(ZonedDateTime.now());
            return entity;
        }).collect(Collectors.toList());
        CopyPortfolio newPortfolio = CopyPortfolio.builder()
            .mlUserId(mlUser)
            .createdAt(ZonedDateTime.now())
            .build();
        CopyPortfolio portfolio = copyPortfolioCustomService.saveEntity(newPortfolio);
        log.info("[uploadPortfolio] newPortfolio: {}", portfolio);
        newDetails.forEach(x -> x.setCopyPortfolioId(portfolio));
        copyPortfolioDetailsCustomRepository.saveAll(newDetails);

        // Trigger copy trading engine
        String triggerId = String.format("[uploadPortfolio]_%s_%s", mlUser.getLogin(), LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd_hh_mm_ss)));
        log.info("[uploadPortfolio] triggerId: {}", triggerId);
        try {
            CopyTradingRequest cancelAllOrderReq = new CopyTradingRequest(mlUser.getId(), triggerId);
            commonService.createKafkaRequest(copyEngineTopic, copyEngineTriggerPortfolioChangedUri, cancelAllOrderReq, "uploadPortfolio", new TypeReference<Response<Void>>() {
            });
        } catch (Exception e) {
            log.error("[uploadPortfolio] triggerID: {}. Error when trigger copy trading engine. Error msg: {}", triggerId, Util.objectToStringJsonIgnoreError(e.getStackTrace()));
        }
    }
}
