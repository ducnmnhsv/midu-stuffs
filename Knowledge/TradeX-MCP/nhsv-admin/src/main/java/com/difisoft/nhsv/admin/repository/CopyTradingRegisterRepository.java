package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyTradingRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the CopyTradingRegister entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CopyTradingRegisterRepository
    extends JpaRepository<CopyTradingRegister, Long>, JpaSpecificationExecutor<CopyTradingRegister> {

    @Modifying
    @Query(value = "UPDATE t_copy_trading_register SET status = :status, updated_at = :updatedAt WHERE account_number = :accountNumber", nativeQuery = true)
    void updateStatusForAccount(@Param("accountNumber") String accountNumber,
                                @Param("status") Boolean status,
                                @Param("updatedAt") ZonedDateTime updatedAt);

    @Query(value = "SELECT * FROM t_copy_trading_register WHERE status = FALSE OR status IS NULL", nativeQuery = true)
    List<CopyTradingRegister> findAccountNumberAndSubAccountForFalseOrNull();

}
