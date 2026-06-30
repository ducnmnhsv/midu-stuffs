package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyTradingOrder;
import com.difisoft.nhsv.admin.domain.enumeration.SellBuyTypeEnum;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
@Primary
public interface CopyTradingOrderCustomRepository extends CopyTradingOrderRepository {
    @Query(value = "select distinct cto from CopyTradingOrder cto " +
        "inner join CopySubscriber cb on cto.copySubscriberId = cb.id " +
        ", CopyPortfolio cp " +
        ", CopyPortfolioHistory cph " +
        "where cto.copySubscriberId = cb.id " +
        "and (cto.copyPortfolioId = cp.id or cto.copyPortfolioId = cph.id) " +
        "and cp.mlUserId.id = cb.mlUserId.id " +
        "and cph.mlUserId.id = cb.mlUserId.id " +
        "and (coalesce(:copyPortfolioId, null ) is null or cto.copyPortfolioId = :copyPortfolioId) " +
        "and cto.copySubscriberId = :subscriberID " +
        "and (coalesce(:fromDate, null) is null or cto.createdAt >= :fromDate) " +
        "and (coalesce(:toDate, null) is null or cto.createdAt <= :toDate) " +
        "and (coalesce(:stockCode, null) is null or cto.symbol = :stockCode) " +
        "and (coalesce(:sellBuyType, null) is null or cto.sellBuyType = :sellBuyType) order by cto.id desc , cto.createdAt desc")
    Page<CopyTradingOrder> findAllByCopyPortfolioIdAndCopySubscriberIdAndOthers(
        @Param("copyPortfolioId") Long copyPortfolioId,
        @Param("subscriberID") Long subscriberID,
        @Param("fromDate") ZonedDateTime fromDate,
        @Param("toDate") ZonedDateTime toDate,
        @Param("stockCode") String stockCode,
        @Param("sellBuyType") SellBuyTypeEnum sellBuyType,
        Pageable pageable
    );
}
