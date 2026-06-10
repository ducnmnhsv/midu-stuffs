package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.Blockholder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomBlockholderRepository extends JpaRepository<Blockholder, Long> {
    @Query("from Blockholder b where b.eKycAdditionalInfo.id = :id")
    List<Blockholder> findByEKycAdditionalInfoId(@Param("id") Long id);
}
