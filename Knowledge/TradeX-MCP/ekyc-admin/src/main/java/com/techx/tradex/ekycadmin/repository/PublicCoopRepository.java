package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.PublicCoop;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the PublicCoop entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PublicCoopRepository extends JpaRepository<PublicCoop, Long> {
    @Query(value = "select pc from PublicCoop pc where pc.eKycAdditionalInfo.id = :eKycAdditionalInfoId order by pc.id")
    List<PublicCoop> findAllByEKycAdditionalInfoId(@Param("eKycAdditionalInfoId") Long eKycAdditionalInfoId);
}
