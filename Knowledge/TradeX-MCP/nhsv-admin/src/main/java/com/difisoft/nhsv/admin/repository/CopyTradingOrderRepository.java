package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyTradingOrder;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopyTradingOrder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CopyTradingOrderRepository extends JpaRepository<CopyTradingOrder, Long>, JpaSpecificationExecutor<CopyTradingOrder> {}
