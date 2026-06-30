package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyPortfolio;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the CopyPortfolio entity.
 */
@Repository
@Primary
public interface CopyPortfolioCustomRepository extends CopyPortfolioRepository {

    @Query(value = "select cp from CopyPortfolio cp where cp.mlUserId.id = :mlUserId")
    Optional<CopyPortfolio> findByMlUserId(@Param("mlUserId") Long mlUserId);

    @Query(value = "select distinct cp from CopyPortfolio cp inner join CopyPortfolioDetails cpd on cp.id = cpd.copyPortfolioId.id where cp.mlUserId.id in :mlUserIds")
    List<CopyPortfolio> findAllByMLUserIdsHasPortfolioDetailsInfo(@Param("mlUserIds") List<Long> mlUserIds);

    @Query(value = "SELECT * FROM t_copy_portfolio WHERE ml_user_id_id = :userId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<CopyPortfolio> findCreatedAtByUserId(@Param("userId") Long userId);
}
