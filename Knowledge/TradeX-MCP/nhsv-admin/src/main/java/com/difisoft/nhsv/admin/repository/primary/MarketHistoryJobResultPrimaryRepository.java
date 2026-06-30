package com.difisoft.nhsv.admin.repository.primary;

import com.difisoft.nhsv.admin.domain.IMarketHistoryJobResultStockEvent;
import com.difisoft.nhsv.admin.domain.MarketHistoryJobResult;
import com.difisoft.nhsv.admin.repository.MarketHistoryJobResultRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
public interface MarketHistoryJobResultPrimaryRepository extends MarketHistoryJobResultRepository {
    @Query(value = "SELECT * FROM market_history_job_result WHERE user_id = :userId ORDER BY time_end DESC LIMIT 1", nativeQuery = true)
    Optional<MarketHistoryJobResult> findLatestJobResult(@Param("userId") Long userId);

    @Query(value = "SELECT " +
        "mhjr.id as id, " +
        "mhjr.symbols as symbols, " +
        "u.id as userId, " +
        "mhjr.isSuccess as isSuccess, " +
        "mhjr.timeStart as timeStart, " +
        "mhjr.timeEnd as timeEnd, " +
        "mhjr.error as error, " +
        "se.id as eventId, " +
        "se.type as eventType, " +
        "se.eventNote as eventName " +
        "FROM MarketHistoryJobResult as mhjr " +
        "LEFT JOIN mhjr.user u " +
        "LEFT JOIN StockEvent as se ON mhjr.eventId = se.id " +
        "WHERE u.id = :userId OR u.id IS NULL")
    Page<IMarketHistoryJobResultStockEvent> findLatestJobResult(@Param("userId") Long userId, Pageable pageable);
}
