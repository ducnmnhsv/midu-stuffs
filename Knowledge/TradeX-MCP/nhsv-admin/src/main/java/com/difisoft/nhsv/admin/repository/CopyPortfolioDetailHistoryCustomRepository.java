package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface CopyPortfolioDetailHistoryCustomRepository extends CopyPortfolioDetailHistoryRepository {
    @Query("SELECT dh FROM CopyPortfolioDetailHistory dh JOIN dh.copyPortfolioHistoryId cp WHERE cp.id = :cpId")
    Page<CopyPortfolioDetailHistory> findByCopyPortfolioIdId(@Param("cpId") Long cpId, Pageable pageable);
}
