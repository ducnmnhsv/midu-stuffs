package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.EContractInfoHistory;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the EContractInfo entity.
 */
@Repository
public interface EContractInfoHistoryRepository extends JpaRepository<EContractInfoHistory, Long>, JpaSpecificationExecutor<EContractInfoHistory> {}
