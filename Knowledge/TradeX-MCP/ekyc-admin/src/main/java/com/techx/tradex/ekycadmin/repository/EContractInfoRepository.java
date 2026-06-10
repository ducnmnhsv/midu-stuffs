package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EContractInfo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the EContractInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EContractInfoRepository extends JpaRepository<EContractInfo, Long>, JpaSpecificationExecutor<EContractInfo> {}
