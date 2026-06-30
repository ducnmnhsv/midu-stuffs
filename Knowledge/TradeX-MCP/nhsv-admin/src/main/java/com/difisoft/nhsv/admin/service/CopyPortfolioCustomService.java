package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.CopyPortfolio;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolio}.
 */
public interface CopyPortfolioCustomService extends CopyPortfolioService {
    Optional<CopyPortfolio> findById(Long copyPortfolioId);
    Optional<CopyPortfolio> findByMLUserId(Long mlUserId);
    List<CopyPortfolio> findAllByMLUserIdsHasPortfolioDetailsInfo(List<Long> mlUserIds);
    CopyPortfolio saveEntity(CopyPortfolio entity);
    Optional<CopyPortfolio> findCreatedAtByUserId(Long userId);
}
