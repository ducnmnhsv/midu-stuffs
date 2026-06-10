package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EKycBankList;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the EKycBankList entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EKycBankListRepository extends JpaRepository<EKycBankList, Long> {
    @Query(value = "select ebl from EKycBankList ebl where ebl.eKyc.id = :eKycId order by ebl.id")
    List<EKycBankList> findAllByEKycId(@Param("eKycId") Long eKycId);
}
