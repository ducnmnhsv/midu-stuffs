package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the EKyc entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EKycRepository extends JpaRepository<EKyc, Long>, JpaSpecificationExecutor<EKyc> {
    Optional<EKyc> findByIdentifierId(String identifierId);

    @Query(value = "select e from EKyc e where e.eKycId = :eKycId")
    Optional<EKyc> findByEKycId(@Param("eKycId") String eKycId);

    @Query(value = "select e from EKyc e where e.id = :id and e.status = :status and e.accountNumber is not null ")
    Optional<EKyc> findByIdAndStatusAndAccountNumberStatus(@Param("id") Long id, @Param("status") Status status);

    @Query(value = "select e from EKyc e where e.status = :status order by e.updatedAt desc")
    List<EKyc> findAllEKycNotUpdateAccountNumber(@Param("status") Status status);

    @Query(
        value = "select e from EKyc e left join EContract ect on e.id = ect.eKyc.id where e.status = :status and ect is null order by e.updatedAt desc"
    )
    List<EKyc> findAllEKycUpdateAccountNumberAndHaveNotInitiatedEContracts(@Param("status") Status status);
}
