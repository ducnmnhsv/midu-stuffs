package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyPortfolioDetails;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the CopyPortfolioDetails entity.
 */
@Repository
@Primary
public interface CopyPortfolioDetailsCustomRepository extends CopyPortfolioDetailsRepository {

    @Query(value = "select cpd from CopyPortfolioDetails cpd inner join CopyPortfolio cp on cpd.copyPortfolioId.id = cp.id inner join User u on u.id = cp.mlUserId.id where cp.mlUserId.id = :mlUserId")
    Page<CopyPortfolioDetails> findAllByMlId(@Param("mlUserId") Long mlUserId, Pageable pageable);

    @Query(value = "select cpd from CopyPortfolioDetails cpd where cpd.copyPortfolioId.id = :copyPortfolioId")
    List<CopyPortfolioDetails> findAllByCopyPortfolioId(@Param("copyPortfolioId") Long copyPortfolioId);

    @Query(value = "select cpd from CopyPortfolioDetails cpd where cpd.copyPortfolioId.id in :copyPortfolioIds")
    List<CopyPortfolioDetails> findAllByCopyPortfolioIds(@Param("copyPortfolioIds") List<Long> copyPortfolioIds);
}
