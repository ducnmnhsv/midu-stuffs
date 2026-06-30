package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopyPortfolioDetailHistory entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CopyPortfolioDetailHistoryRepository
    extends JpaRepository<CopyPortfolioDetailHistory, Long>, JpaSpecificationExecutor<CopyPortfolioDetailHistory> {}
