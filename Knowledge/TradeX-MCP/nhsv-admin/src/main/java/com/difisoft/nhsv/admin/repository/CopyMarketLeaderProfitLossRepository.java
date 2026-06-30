package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLoss;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopyMarketLeaderProfitLoss entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CopyMarketLeaderProfitLossRepository extends JpaRepository<CopyMarketLeaderProfitLoss, Long> {
    @Query(
        "select copyMarketLeaderProfitLoss from CopyMarketLeaderProfitLoss copyMarketLeaderProfitLoss where copyMarketLeaderProfitLoss.mlUserId.login = ?#{principal.username}"
    )
    List<CopyMarketLeaderProfitLoss> findByMlUserIdIsCurrentUser();
}
