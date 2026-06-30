package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyPortfolio;
import com.difisoft.nhsv.admin.repository.CopyPortfolioCustomRepository;
import com.difisoft.nhsv.admin.repository.CopyPortfolioRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioCustomService;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link CopyPortfolio}.
 */
@Service("copyPortfolioCustomService")
@Transactional
@Primary
public class CopyPortfolioCustomServiceImpl extends CopyPortfolioServiceImpl implements CopyPortfolioCustomService {

    private final CopyPortfolioCustomRepository copyPortfolioCustomRepository;
    private final CopyPortfolioMapper copyPortfolioMapper;

    @Autowired
    public CopyPortfolioCustomServiceImpl(CopyPortfolioRepository copyPortfolioRepository, CopyPortfolioMapper copyPortfolioMapper, CopyPortfolioCustomRepository copyPortfolioCustomRepository, CopyPortfolioMapper copyPortfolioMapper1) {
        super(copyPortfolioRepository, copyPortfolioMapper);
        this.copyPortfolioCustomRepository = copyPortfolioCustomRepository;
        this.copyPortfolioMapper = copyPortfolioMapper1;
    }

    @Override
    public Optional<CopyPortfolio> findById(Long copyPortfolioId) {
        return copyPortfolioCustomRepository.findById(copyPortfolioId);
    }

    @Override
    public Optional<CopyPortfolio> findByMLUserId(Long mlUserId) {
        return copyPortfolioCustomRepository.findByMlUserId(mlUserId);
    }

    @Override
    public List<CopyPortfolio> findAllByMLUserIdsHasPortfolioDetailsInfo(List<Long> mlUserIds) {
        return copyPortfolioCustomRepository.findAllByMLUserIdsHasPortfolioDetailsInfo(mlUserIds);
    }

    @Override
    public CopyPortfolio saveEntity(CopyPortfolio entity) {
        return copyPortfolioCustomRepository.save(entity);
    }

    @Override
    public Optional<CopyPortfolio> findCreatedAtByUserId(Long userId) {
        return copyPortfolioCustomRepository.findCreatedAtByUserId(userId);
    }
}
