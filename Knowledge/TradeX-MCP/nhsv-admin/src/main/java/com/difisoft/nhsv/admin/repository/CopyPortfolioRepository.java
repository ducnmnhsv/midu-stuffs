package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyPortfolio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopyPortfolio entity.
 */
@Repository
public interface CopyPortfolioRepository extends JpaRepository<CopyPortfolio, Long>, JpaSpecificationExecutor<CopyPortfolio> {
    @Query("select copyPortfolio from CopyPortfolio copyPortfolio where copyPortfolio.mlUserId.login = ?#{principal.username}")
    List<CopyPortfolio> findByMlUserIdIsCurrentUser();

    default Optional<CopyPortfolio> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CopyPortfolio> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CopyPortfolio> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct copyPortfolio from CopyPortfolio copyPortfolio left join fetch copyPortfolio.mlUserId",
        countQuery = "select count(distinct copyPortfolio) from CopyPortfolio copyPortfolio"
    )
    Page<CopyPortfolio> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct copyPortfolio from CopyPortfolio copyPortfolio left join fetch copyPortfolio.mlUserId")
    List<CopyPortfolio> findAllWithToOneRelationships();

    @Query("select copyPortfolio from CopyPortfolio copyPortfolio left join fetch copyPortfolio.mlUserId where copyPortfolio.id =:id")
    Optional<CopyPortfolio> findOneWithToOneRelationships(@Param("id") Long id);
}
