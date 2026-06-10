package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.Blockholder;
import com.techx.tradex.ekycadmin.domain.PublicCoop;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Blockholder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BlockholderRepository extends JpaRepository<Blockholder, Long> {
    @Query(value = "select bl from Blockholder bl where bl.eKycAdditionalInfo.id = :eKycAdditionalInfoId order by bl.id")
    List<Blockholder> findAllByEKycAdditionalInfoId(@Param("eKycAdditionalInfoId") Long eKycAdditionalInfoId);
}
