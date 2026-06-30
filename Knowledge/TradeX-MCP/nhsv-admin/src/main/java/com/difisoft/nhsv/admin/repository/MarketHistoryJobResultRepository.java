package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.MarketHistoryJobResult;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MarketHistoryJobResult entity.
 */
@Repository
public interface MarketHistoryJobResultRepository extends JpaRepository<MarketHistoryJobResult, Long> {
    @Query(
        "select marketHistoryJobResult from MarketHistoryJobResult marketHistoryJobResult where marketHistoryJobResult.user.login = ?#{principal.username}"
    )
    List<MarketHistoryJobResult> findByUserIsCurrentUser();

    default Optional<MarketHistoryJobResult> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<MarketHistoryJobResult> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<MarketHistoryJobResult> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct marketHistoryJobResult from MarketHistoryJobResult marketHistoryJobResult left join fetch marketHistoryJobResult.user",
        countQuery = "select count(distinct marketHistoryJobResult) from MarketHistoryJobResult marketHistoryJobResult"
    )
    Page<MarketHistoryJobResult> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select distinct marketHistoryJobResult from MarketHistoryJobResult marketHistoryJobResult left join fetch marketHistoryJobResult.user"
    )
    List<MarketHistoryJobResult> findAllWithToOneRelationships();

    @Query(
        "select marketHistoryJobResult from MarketHistoryJobResult marketHistoryJobResult left join fetch marketHistoryJobResult.user where marketHistoryJobResult.id =:id"
    )
    Optional<MarketHistoryJobResult> findOneWithToOneRelationships(@Param("id") Long id);
}
