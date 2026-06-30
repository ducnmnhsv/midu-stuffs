package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.Broker;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Broker entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BrokerRepository extends JpaRepository<Broker, Long>, JpaSpecificationExecutor<Broker> {}
