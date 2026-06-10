package com.techx.tradex.ekycadmin.service.impl;

import com.techx.tradex.ekycadmin.domain.EContractInfo;
import com.techx.tradex.ekycadmin.repository.EContractInfoRepository;
import com.techx.tradex.ekycadmin.service.EContractInfoCustomService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link EContractInfo}.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Primary
public class EContractInfoCustomServiceImpl implements EContractInfoCustomService {

    private final Logger log = LoggerFactory.getLogger(EContractInfoCustomServiceImpl.class);

    private final EContractInfoRepository eContractInfoRepository;

    @Override
    @Transactional
    public EContractInfo save(EContractInfo eContractInfo) {
        return eContractInfoRepository.save(eContractInfo);
    }
}
