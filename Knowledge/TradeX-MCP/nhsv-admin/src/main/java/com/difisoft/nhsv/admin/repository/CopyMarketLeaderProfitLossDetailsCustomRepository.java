package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLoss;
import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLossDetails;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Primary
@Repository
public interface CopyMarketLeaderProfitLossDetailsCustomRepository extends CopyMarketLeaderProfitLossDetailsRepository {

    @Query(value = "select mlPLD from CopyMarketLeaderProfitLossDetails mlPLD where mlPLD.copyMarketLeaderProfitLossId.id in :copyMarketLeaderProfitLossIds ")
    List<CopyMarketLeaderProfitLossDetails> findAllByCopyMarketLeaderProfitLossIds(@Param("copyMarketLeaderProfitLossIds") List<Long> copyMarketLeaderProfitLossIds);

    @Query(value = "select mlPLD from CopyMarketLeaderProfitLossDetails mlPLD where mlPLD.reportDate >= :startReportDate and mlPLD.reportDate <= :endReportDate and mlPLD.copyMarketLeaderProfitLossId.id in :copyMarketLeaderProfitLossIds ")
    List<CopyMarketLeaderProfitLossDetails> findAllByCopyMarketLeaderProfitLossIdAndReportDate(
        @Param("copyMarketLeaderProfitLossIds") List<Long> copyMarketLeaderProfitLossIds
        , @Param("startReportDate") ZonedDateTime startReportDate
        , @Param("endReportDate") ZonedDateTime endReportDate
    );

    @Query(value = "delete from CopyMarketLeaderProfitLossDetails mlPLD where mlPLD.copyMarketLeaderProfitLossId.id in :profitLossIdsRemoved")
    @Modifying
    void deleteAllByProfitLossIds(@Param("profitLossIdsRemoved") List<Long> profitLossIdsRemoved);
}
