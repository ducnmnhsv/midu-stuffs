package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory;
import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailHistoryCustomRepository;
import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailHistoryRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailHistoryCustomService;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailHistoryDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioDetailHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("copyPortfolioDetailHistoryCustomService")
@Slf4j
@Primary
@Transactional
public class CopyPortfolioDetailHistoryCustomServiceImpl extends CopyPortfolioDetailHistoryServiceImpl implements CopyPortfolioDetailHistoryCustomService {
    private final CopyPortfolioDetailHistoryCustomRepository copyPortfolioDetailHistoryCustomRepository;
    private final CopyPortfolioDetailHistoryMapper copyPortfolioDetailHistoryMapper;

    @Autowired
    public CopyPortfolioDetailHistoryCustomServiceImpl(CopyPortfolioDetailHistoryRepository copyPortfolioDetailHistoryRepository, CopyPortfolioDetailHistoryMapper copyPortfolioDetailHistoryMapper, CopyPortfolioDetailHistoryCustomRepository copyPortfolioDetailHistoryCustomRepository) {
        super(copyPortfolioDetailHistoryRepository, copyPortfolioDetailHistoryMapper);
        this.copyPortfolioDetailHistoryCustomRepository = copyPortfolioDetailHistoryCustomRepository;
        this.copyPortfolioDetailHistoryMapper = copyPortfolioDetailHistoryMapper;
    }


    @Override
    public Page<CopyPortfolioDetailHistoryDTO> findAllByCopyPortfolioIdId(Long cpId, Pageable pageable) {
        return copyPortfolioDetailHistoryCustomRepository.findByCopyPortfolioIdId(cpId, pageable).map(copyPortfolioDetailHistoryMapper::toDto);
    }

    @Override
    public void saveAll(List<CopyPortfolioDetailHistory> entities) {
        copyPortfolioDetailHistoryCustomRepository.saveAll(entities);
    }
}
