package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyPortfolioDetails;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopyPortfolioDetails entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CopyPortfolioDetailsRepository
    extends JpaRepository<CopyPortfolioDetails, Long>, JpaSpecificationExecutor<CopyPortfolioDetails> {}
