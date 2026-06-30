package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopySubscriberHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopySubscriberHistory entity.
 */
@Repository
public interface CopySubscriberHistoryRepository
    extends JpaRepository<CopySubscriberHistory, Long>, JpaSpecificationExecutor<CopySubscriberHistory> {
    @Query(
        "select copySubscriberHistory from CopySubscriberHistory copySubscriberHistory where copySubscriberHistory.mlUserId.login = ?#{principal.username}"
    )
    List<CopySubscriberHistory> findByMlUserIdIsCurrentUser();

    default Optional<CopySubscriberHistory> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CopySubscriberHistory> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CopySubscriberHistory> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct copySubscriberHistory from CopySubscriberHistory copySubscriberHistory left join fetch copySubscriberHistory.mlUserId",
        countQuery = "select count(distinct copySubscriberHistory) from CopySubscriberHistory copySubscriberHistory"
    )
    Page<CopySubscriberHistory> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select distinct copySubscriberHistory from CopySubscriberHistory copySubscriberHistory left join fetch copySubscriberHistory.mlUserId"
    )
    List<CopySubscriberHistory> findAllWithToOneRelationships();

    @Query(
        "select copySubscriberHistory from CopySubscriberHistory copySubscriberHistory left join fetch copySubscriberHistory.mlUserId where copySubscriberHistory.id =:id"
    )
    Optional<CopySubscriberHistory> findOneWithToOneRelationships(@Param("id") Long id);
}
