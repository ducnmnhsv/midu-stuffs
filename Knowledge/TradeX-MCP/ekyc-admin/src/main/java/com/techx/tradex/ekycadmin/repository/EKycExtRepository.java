package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EKycExt;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the EKycExt entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EKycExtRepository extends JpaRepository<EKycExt, Long> {}
