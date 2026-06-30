package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopyMarketLeaderDetails entity.
 */
@Repository
public interface CopyMarketLeaderDetailsRepository
    extends JpaRepository<CopyMarketLeaderDetails, Long>, JpaSpecificationExecutor<CopyMarketLeaderDetails> {
    @Query(
        "select copyMarketLeaderDetails from CopyMarketLeaderDetails copyMarketLeaderDetails where copyMarketLeaderDetails.mlUserId.login = ?#{principal.username}"
    )
    List<CopyMarketLeaderDetails> findByMlUserIdIsCurrentUser();

    default Optional<CopyMarketLeaderDetails> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CopyMarketLeaderDetails> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CopyMarketLeaderDetails> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct copyMarketLeaderDetails from CopyMarketLeaderDetails copyMarketLeaderDetails left join fetch copyMarketLeaderDetails.mlUserId",
        countQuery = "select count(distinct copyMarketLeaderDetails) from CopyMarketLeaderDetails copyMarketLeaderDetails"
    )
    Page<CopyMarketLeaderDetails> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select distinct copyMarketLeaderDetails from CopyMarketLeaderDetails copyMarketLeaderDetails left join fetch copyMarketLeaderDetails.mlUserId"
    )
    List<CopyMarketLeaderDetails> findAllWithToOneRelationships();

    @Query(
        "select copyMarketLeaderDetails from CopyMarketLeaderDetails copyMarketLeaderDetails left join fetch copyMarketLeaderDetails.mlUserId where copyMarketLeaderDetails.id =:id"
    )
    Optional<CopyMarketLeaderDetails> findOneWithToOneRelationships(@Param("id") Long id);
}
