package com.techx.tradex.ekycadmin.repository;

import java.util.Optional;

import com.techx.tradex.ekycadmin.models.dto.IEContractInfo;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techx.tradex.ekycadmin.domain.EContractInfo;

/**
 * Spring Data SQL repository for the EContractInfo entity.
 */
@Repository
@Primary
public interface EContractInfoCustomRepository extends EContractInfoRepository {
    @Query(value = "SELECT * FROM econtract_info e WHERE e.e_contract_id = :id", nativeQuery = true)
    Optional<EContractInfo> findByEContractId(@Param("id") Long id);

    @Query("select e.id as id, e.contractStatus as contractStatus, e.customerSignatueStatus as customerSignatueStatus, " +
        " e.securitiesSignatureStatus as securitiesSignatureStatus, e.eContract as econtract, e.signFileContent as signFileContent " +
        " from EContractInfo e join e.eContract c where c.eKyc.id = :id")
    Optional<IEContractInfo> findByEKycId(@Param("id") Long id);

    @Query("select requestData as requestData, contractFileContent as contractFileContent from EContractInfo where id = :id")
    Optional<IEContractInfo> findRequestDataContractFileContentById(@Param("id") Long id);
}
