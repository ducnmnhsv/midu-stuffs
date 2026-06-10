package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EKycCreatorStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the EKycCreatorStatus entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EKycCreatorStatusRepository extends JpaRepository<EKycCreatorStatus, Long> {}
