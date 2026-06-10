package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EKycBankList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomEKycBankListRepository extends JpaRepository<EKycBankList, Long> {
    @Query("from EKycBankList e where e.eKyc.id = :id")
    List<EKycBankList> findByEKycId(@Param("id") Long id);
}
