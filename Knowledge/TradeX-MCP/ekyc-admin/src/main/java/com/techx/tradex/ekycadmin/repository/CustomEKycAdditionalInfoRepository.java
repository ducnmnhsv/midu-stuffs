package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EKycAdditionalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomEKycAdditionalInfoRepository extends JpaRepository<EKycAdditionalInfo, Long> {
    @Query("from EKycAdditionalInfo e where e.eKyc.id = :id")
    Optional<EKycAdditionalInfo> findByEKycId(Long id);
}
