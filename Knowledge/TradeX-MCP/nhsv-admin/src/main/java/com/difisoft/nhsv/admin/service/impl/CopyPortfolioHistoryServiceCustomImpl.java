package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyPortfolioHistory;
import com.difisoft.nhsv.admin.repository.CopyPortfolioHistoryCustomRepository;
import com.difisoft.nhsv.admin.repository.CopyPortfolioHistoryRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioHistoryCustomService;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioHistoryDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service("copyPortfolioHistoryCustomService")
@Slf4j
@Primary
@Transactional
public class CopyPortfolioHistoryServiceCustomImpl extends CopyPortfolioHistoryServiceImpl implements CopyPortfolioHistoryCustomService {
    private final CopyPortfolioHistoryCustomRepository copyPortfolioHistoryCustomRepository;
    private final CopyPortfolioHistoryMapper copyPortfolioHistoryMapper;

    @Autowired
    public CopyPortfolioHistoryServiceCustomImpl(CopyPortfolioHistoryRepository copyPortfolioHistoryRepository, CopyPortfolioHistoryMapper copyPortfolioHistoryMapper, CopyPortfolioHistoryCustomRepository copyPortfolioHistoryCustomRepository) {
        super(copyPortfolioHistoryRepository, copyPortfolioHistoryMapper);
        this.copyPortfolioHistoryCustomRepository = copyPortfolioHistoryCustomRepository;
        this.copyPortfolioHistoryMapper = copyPortfolioHistoryMapper;
    }


    @Override
    public Page<CopyPortfolioHistoryDTO> findAllByMlUserIdId(Long cpId, Date fromDate, Date toDate, Pageable pageable) {
        return copyPortfolioHistoryCustomRepository.findByMlUserIdId(cpId, fromDate, toDate, pageable).map(copyPortfolioHistoryMapper::toDto);
    }

    @Override
    public CopyPortfolioHistory save(CopyPortfolioHistory entity) {
        return copyPortfolioHistoryCustomRepository.save(entity);
    }
}
