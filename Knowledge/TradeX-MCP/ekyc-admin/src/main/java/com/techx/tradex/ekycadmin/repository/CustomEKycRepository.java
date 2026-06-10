package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EKyc;
import java.util.List;

import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * Spring Data SQL repository for the EKyc entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CustomEKycRepository extends JpaRepository<EKyc, Long>, JpaSpecificationExecutor<EKyc> {
    List<EKyc> findByIdentifierIdOrPhoneNo(String identifierNumber, String phoneNo);
    List<EKyc> findByIdIn(List<Long> ids);
    List<EKyc> findByIdentifierId(String identifierNumber);

    @Query(value = "FROM EKyc WHERE identifierId = :identifierId OR eKycId = :eKycId")
    List<EKyc> findByIdentifierIdOrEKycId(@Param("identifierId") String identifierId, @Param("eKycId") String eKycId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update EKyc e set e.status = :status where e.status = 'PENDING'")
    void updateStatus(@Param("status") Status status);
}
