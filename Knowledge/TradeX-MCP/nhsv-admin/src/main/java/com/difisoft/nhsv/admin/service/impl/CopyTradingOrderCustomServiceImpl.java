package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyTradingOrder;
import com.difisoft.nhsv.admin.domain.enumeration.SellBuyTypeEnum;
import com.difisoft.nhsv.admin.domain.request.CopyTradingOrderRequest;
import com.difisoft.nhsv.admin.repository.CopyTradingOrderCustomRepository;
import com.difisoft.nhsv.admin.repository.CopyTradingOrderRepository;
import com.difisoft.nhsv.admin.service.CopyTradingOrderCustomService;
import com.difisoft.nhsv.admin.service.dto.CopyTradingOrderDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyTradingOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Objects;

@Service("copyTradingOrderCustomService")
@Slf4j
@Primary
@Transactional
public class CopyTradingOrderCustomServiceImpl extends CopyTradingOrderServiceImpl implements CopyTradingOrderCustomService {

    private final CopyTradingOrderCustomRepository copyTradingOrderCustomRepository;
    private final CopyTradingOrderMapper copyTradingOrderMapper;

    @Autowired
    public CopyTradingOrderCustomServiceImpl(
        CopyTradingOrderRepository copyTradingOrderRepository
        , CopyTradingOrderMapper copyTradingOrderMapper
        , CopyTradingOrderCustomRepository copyTradingOrderCustomRepository) {
        super(copyTradingOrderRepository, copyTradingOrderMapper);
        this.copyTradingOrderCustomRepository = copyTradingOrderCustomRepository;
        this.copyTradingOrderMapper = copyTradingOrderMapper;
    }

    @Override
    public Page<CopyTradingOrderDTO> findAllByCopyPortfolioIdAndCopySubscriberIdAndOthers(
        CopyTradingOrderRequest request
        , Pageable pageable
    ) {
        Page<CopyTradingOrder> results = copyTradingOrderCustomRepository.findAllByCopyPortfolioIdAndCopySubscriberIdAndOthers(
            request.getCopyPortfolioId(),
            request.getSubScriberId(),
            Objects.isNull(request.getFromDate())
                ? null
                : request.getFromDate().atZone(ZoneId.systemDefault()),
            Objects.isNull(request.getToDate())
                ? null
                : request.getToDate().atZone(ZoneId.systemDefault()),
            StringUtils.isBlank(request.getStockCode()) ? null : request.getStockCode(),
            StringUtils.isBlank(request.getSellBuyType()) ? null : SellBuyTypeEnum.valueOf(request.getSellBuyType()),
            pageable
        );
        log.info("[findAllByCopyPortfolioIdAndCopySubscriberIdAndOthers] results: {}", results);
        return results.map(copyTradingOrderMapper::toDto);
    }
}
