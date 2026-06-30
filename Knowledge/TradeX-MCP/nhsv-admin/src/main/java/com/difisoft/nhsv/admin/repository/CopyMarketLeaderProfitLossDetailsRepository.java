package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLossDetails;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopyMarketLeaderProfitLossDetails entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CopyMarketLeaderProfitLossDetailsRepository extends JpaRepository<CopyMarketLeaderProfitLossDetails, Long> {
    @Query(
        "select copyMarketLeaderProfitLossDetails from CopyMarketLeaderProfitLossDetails copyMarketLeaderProfitLossDetails where copyMarketLeaderProfitLossDetails.mlUserId.login = ?#{principal.username}"
    )
    List<CopyMarketLeaderProfitLossDetails> findByMlUserIdIsCurrentUser();
}
