package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.PublicCoop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomPublicCoopRepository extends JpaRepository<PublicCoop, Long> {
    @Query("from PublicCoop p where p.eKycAdditionalInfo.id = :id")
    List<PublicCoop> findByEKycAdditionalInfoId(@Param("id") Long id);
}
