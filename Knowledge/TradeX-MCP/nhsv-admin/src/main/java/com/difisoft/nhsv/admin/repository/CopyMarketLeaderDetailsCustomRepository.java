package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails;
import com.difisoft.nhsv.admin.domain.User;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the CopyMarketLeaderDetails entity.
 */
@Repository
@Primary
public interface CopyMarketLeaderDetailsCustomRepository extends CopyMarketLeaderDetailsRepository {

    @Query(value = "select cmd from CopyMarketLeaderDetails cmd inner join User u on cmd.mlUserId.id = u.id " +
        "where cmd.mlUserId.id in :mlUserIds " +
        "and (coalesce(:fromDate, null) is null or cmd.createdAt >= :fromDate) " +
        "and (coalesce(:toDate, null) is null or cmd.createdAt <= :toDate) " +
        "and (coalesce(:type, null ) is null or cmd.type = :type) " +
        "and (coalesce(:label, null ) is null or cmd.label = :label) " +
        "and (coalesce(:key, null ) is null or cmd.key = :key) ")
    Page<CopyMarketLeaderDetails> findAllByMlIdsAndDateRangeAndConditions(
        @Param("mlUserIds") List<Long> mlUserIds
        , @Param("fromDate") ZonedDateTime fromDate
        , @Param("toDate") ZonedDateTime toDate
        , @Param("type") String type
        , @Param("label") String label
        , @Param("key") String key
        , Pageable pageable
    );

    @Query(value = "select cmd from CopyMarketLeaderDetails cmd inner join User u on cmd.mlUserId.id = u.id " +
        "where cmd.mlUserId.id in :mlUserIds and (cmd.mlUserId.id, cmd.createdAt, cmd.type, cmd.label, cmd.key) " +
        "IN (" +
        "select cmld.mlUserId.id, MAX(cmld.createdAt), cmld.type, cmld.label, cmld.key " +
        "from CopyMarketLeaderDetails cmld " +
        "where (coalesce(:type, null ) is null or cmld.type = :type) " +
        "and (coalesce(:label, null ) is null or cmld.label = :label) " +
        "and (coalesce(:key, null ) is null or cmld.key = :key) " +
        "group by cmld.mlUserId.id, cmld.type, cmld.label, cmld.key" +
        ")")
    List<CopyMarketLeaderDetails> findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(
        @Param("mlUserIds") List<Long> mlUserIds
        , @Param("type") String type
        , @Param("label") String label
        , @Param("key") String key
        , Sort sort
    );

    @Query(value = "INSERT INTO t_copy_market_leader_details(created_at, updated_at, ml_user_id_id, type, label, jhi_key, value) " +
        "SELECT UTC_TIMESTAMP(), UTC_TIMESTAMP(), tcm.ml_user_id_id, 'COPY_TRADING', 'MARKET_LEADER_SUMMARY_INFO', 'TOTAL_SUB', coalesce(sub_data.total, 0) " +
        "FROM (" +
        "SELECT distinct (u.id) AS user_id " +
        "FROM t_user u INNER JOIN t_user_authority tua " +
        "ON u.id = tua.user_id " +
        "WHERE activated = true " +
        "AND authority_name = 'MARKET_LEADER'" +
        ") AS user_data " +
        "INNER JOIN (" +
        "SELECT distinct ml_user_id_id, COUNT(ml_user_id_id) AS total " +
        "FROM t_copy_subscriber GROUP BY ml_user_id_id" +
        ") AS sub_data " +
        "ON user_data.user_id = sub_data.ml_user_id_id " +
        "right join " +
        "(select distinct (ml_user_id_id) from t_copy_market_leader_details where jhi_key = 'TOTAL_SUB' and value > 0 ) tcm " +
        "on sub_data.ml_user_id_id = tcm.ml_user_id_id ", nativeQuery = true)
    @Modifying
    void saveTotalSubscriberForAllMarketLeader();

    @Modifying
    @Query(value = "delete from CopyMarketLeaderDetails cmd where DATE_FORMAT(cmd.createdAt, '%Y%m%d') = :calcDate " +
        "and (cmd.type = :type) and (cmd.label = :label) and (cmd.key = :key)")
    void deleteAllTotalSubscriberData(
        @Param("calcDate") String calcDate
        , @Param("type") String type
        , @Param("label") String label
        , @Param("key") String key
    );

    @Modifying
    void deleteByMlUserId(User userDTOToUser);
}
