package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.domain.EKycCreatorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the EKycCreatorStatus entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CustomEKycCreatorStatusRepository extends JpaRepository<EKycCreatorStatus, Long> {
    List<EKycCreatorStatus> findByIdIn(List<Long> ids);
}
