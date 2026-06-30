package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopySubscriber;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopySubscriber entity.
 */
@Repository
public interface CopySubscriberRepository extends JpaRepository<CopySubscriber, Long>, JpaSpecificationExecutor<CopySubscriber> {
    @Query("select copySubscriber from CopySubscriber copySubscriber where copySubscriber.mlUserId.login = ?#{principal.username}")
    List<CopySubscriber> findByMlUserIdIsCurrentUser();

    default Optional<CopySubscriber> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CopySubscriber> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CopySubscriber> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct copySubscriber from CopySubscriber copySubscriber left join fetch copySubscriber.mlUserId",
        countQuery = "select count(distinct copySubscriber) from CopySubscriber copySubscriber"
    )
    Page<CopySubscriber> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct copySubscriber from CopySubscriber copySubscriber left join fetch copySubscriber.mlUserId")
    List<CopySubscriber> findAllWithToOneRelationships();

    @Query("select copySubscriber from CopySubscriber copySubscriber left join fetch copySubscriber.mlUserId where copySubscriber.id =:id")
    Optional<CopySubscriber> findOneWithToOneRelationships(@Param("id") Long id);
}
