package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.repository.CopyMarketLeaderProfitLossDetailsCustomRepository;
import com.difisoft.nhsv.admin.service.CopyMarketLeaderProfitLossDetailsCustomService;
import com.difisoft.nhsv.admin.service.mapper.CopyMarketLeaderProfitLossDetailsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
@Slf4j
@Transactional
public class CopyMarketLeaderProfitLossDetailsCustomServiceImpl implements CopyMarketLeaderProfitLossDetailsCustomService {
    private final CopyMarketLeaderProfitLossDetailsCustomRepository copyMarketLeaderProfitLossDetailsCustomRepository;
    private final CopyMarketLeaderProfitLossDetailsMapper copyMarketLeaderProfitLossDetailsMapper;

    @Autowired
    public CopyMarketLeaderProfitLossDetailsCustomServiceImpl(
        CopyMarketLeaderProfitLossDetailsCustomRepository copyMarketLeaderProfitLossDetailsCustomRepository
        , CopyMarketLeaderProfitLossDetailsMapper copyMarketLeaderProfitLossDetailsMapper
    ) {
        this.copyMarketLeaderProfitLossDetailsCustomRepository = copyMarketLeaderProfitLossDetailsCustomRepository;
        this.copyMarketLeaderProfitLossDetailsMapper = copyMarketLeaderProfitLossDetailsMapper;
    }

}
