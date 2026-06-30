package com.difisoft.nhsv.admin.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface CopyTradingRegisterCustomRepository extends CopyTradingRegisterRepository {

    @Query("SELECT COUNT(t) FROM CopyTradingRegister t WHERE t.accountNumber = :accountNumber AND t.subAccount = :subAccount")
    long countByAccountNumberAndSubAccount(@Param("accountNumber") String accountNumber, @Param("subAccount") String subAccount);
}
