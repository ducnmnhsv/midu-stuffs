package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLoss;
import com.difisoft.nhsv.admin.domain.User;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Repository
@Primary
public interface CopyMarketLeaderProfitLossCustomRepository extends CopyMarketLeaderProfitLossRepository {

    @Query(value = "select mlPL from CopyMarketLeaderProfitLoss mlPL where mlPL.mlUserId.id = :marketLeaderId and mlPL.type = :type " +
        "and (coalesce(:fromDate, null) is null or mlPL.reportDate >= :fromDate) " +
        "and (coalesce(:toDate, null) is null or mlPL.reportDate <= :toDate) ")
    Page<CopyMarketLeaderProfitLoss> findByMlUserIdAndReportDate(
        @Param("marketLeaderId") Long marketLeaderId
        , @Param("fromDate") ZonedDateTime fromDate
        , @Param("toDate") ZonedDateTime toDate
        , @Param("type") String type
        , @Param("pageable") Pageable pageable
    );

    @Query(value = "select mlPL from CopyMarketLeaderProfitLoss mlPL where " +
        "(coalesce(:marketLeaderIds , null ) is null or mlPL.mlUserId.id in :marketLeaderIds) " +
        "and (coalesce(:fromDate, null) is null or mlPL.reportDate >= :fromDate) " +
        "and (coalesce(:toDate, null) is null or mlPL.reportDate <= :toDate) " +
        "order by mlPL.reportDate desc, mlPL.mlUserId.id asc")
    List<CopyMarketLeaderProfitLoss> findByMlUserIdsAndReportDatePeriod(
        @NotNull @Param("marketLeaderIds") List<Long> marketLeaderIds
        , @Param("fromDate") ZonedDateTime fromDate
        , @Param("toDate") ZonedDateTime toDate
    );

    @Query(value = "select mlPL from CopyMarketLeaderProfitLoss mlPL where mlPL.mlUserId = :mlUser and mlPL.reportDate < :reportDate and type = 'DAY' order by mlPL.reportDate desc, mlPL.mlUserId.id desc ")
    List<CopyMarketLeaderProfitLoss> findAllByMlUserIdAndReportDateIsLessThanOrderByReportDateDesc(@Param("mlUser") User mlUser, @Param("reportDate") ZonedDateTime reportDate);

    @Query(value = "select mlPL from CopyMarketLeaderProfitLoss mlPL where mlPL.reportDate >= :startReportDate and mlPL.reportDate <= :endReportDate and mlPL.mlUserId.id IN :mlUserIds")
    List<CopyMarketLeaderProfitLoss> findAllByMlUserIdAndReportDate(
        @Param("mlUserIds") List<Long> mlUserIds
        , @Param("startReportDate") ZonedDateTime startReportDate
        , @Param("endReportDate") ZonedDateTime endReportDate
    );
    @Query(value = "select distinct tcmlpl " +
        "from CopyMarketLeaderProfitLoss tcmlpl " +
        "inner join User tu on tcmlpl.mlUserId.id = tu.id " +
        "inner join tu.authorities ta " +
        "inner join CopyMarketLeaderDetails tcmld on tu.id = tcmld.mlUserId.id " +
        "where ta.name = :authName " +
        "  and tu.activated = :activated " +
        "  and tcmld.type = :type " +
        "  and tcmld.label = :label " +
        "  and tcmld.key = :key " +
        "  and tcmlpl.reportDate < STR_TO_DATE(tcmld.value, '%d/%m/%Y %H:%i:%s.%f') ")
    List<CopyMarketLeaderProfitLoss> getAllProfitLossBeforeBeMarketLeaderDate(
        @Param("authName") String authName
        , @Param("activated") boolean activated
        , @Param("type") String type
        , @Param("label") String label
        , @Param("key") String key
    );

    @Query(value = "select mlPL from CopyMarketLeaderProfitLoss mlPL where mlPL.reportDate < :reportDate order by mlPL.reportDate desc, mlPL.mlUserId.id desc ")
    List<CopyMarketLeaderProfitLoss> findAllByReportDate(@Param("reportDate") String reportDate);

    @Query(value = "select mlPL from CopyMarketLeaderProfitLoss mlPL where mlPL.mlUserId.id = :id and mlPL.type = :period")
    List<CopyMarketLeaderProfitLoss> findByMlUserIdAndType(@Param("id") Long id,@Param("period") String period, Pageable pageable);
}

