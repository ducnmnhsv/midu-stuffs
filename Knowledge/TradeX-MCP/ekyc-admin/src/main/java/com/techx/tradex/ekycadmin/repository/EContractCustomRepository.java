package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EContract;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface EContractCustomRepository extends EContractRepository {
    Optional<EContract> findByRefIdAndEnvelopeId(String refId, String envelopId);

    @Query(value = "select ect from EContract ect where ect.eKyc.id = :eKycId")
    Optional<EContract> findByEKycTableId(@Param("eKycId") Long eKycId);

    @Query("from EContract e where e.eKyc.id = :id")
    Optional<EContract> findByEKycId(@Param("id") Long id);
}
