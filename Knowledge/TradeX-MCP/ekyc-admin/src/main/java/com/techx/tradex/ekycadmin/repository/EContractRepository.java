package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EContract;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the EContract entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EContractRepository extends JpaRepository<EContract, Long>, JpaSpecificationExecutor<EContract> {}
