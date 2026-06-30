package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyPortfolioHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopyPortfolioHistory entity.
 */
@Repository
public interface CopyPortfolioHistoryRepository
    extends JpaRepository<CopyPortfolioHistory, Long>, JpaSpecificationExecutor<CopyPortfolioHistory> {
    @Query(
        "select copyPortfolioHistory from CopyPortfolioHistory copyPortfolioHistory where copyPortfolioHistory.mlUserId.login = ?#{principal.username}"
    )
    List<CopyPortfolioHistory> findByMlUserIdIsCurrentUser();

    default Optional<CopyPortfolioHistory> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CopyPortfolioHistory> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CopyPortfolioHistory> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct copyPortfolioHistory from CopyPortfolioHistory copyPortfolioHistory left join fetch copyPortfolioHistory.mlUserId",
        countQuery = "select count(distinct copyPortfolioHistory) from CopyPortfolioHistory copyPortfolioHistory"
    )
    Page<CopyPortfolioHistory> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select distinct copyPortfolioHistory from CopyPortfolioHistory copyPortfolioHistory left join fetch copyPortfolioHistory.mlUserId"
    )
    List<CopyPortfolioHistory> findAllWithToOneRelationships();

    @Query(
        "select copyPortfolioHistory from CopyPortfolioHistory copyPortfolioHistory left join fetch copyPortfolioHistory.mlUserId where copyPortfolioHistory.id =:id"
    )
    Optional<CopyPortfolioHistory> findOneWithToOneRelationships(@Param("id") Long id);
}
