package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EKycAdditionalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the EKycAdditionalInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EKycAdditionalInfoRepository extends JpaRepository<EKycAdditionalInfo, Long> {
}
