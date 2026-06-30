package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopySubscriberDetails;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CopySubscriberDetails entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CopySubscriberDetailsRepository
    extends JpaRepository<CopySubscriberDetails, Long>, JpaSpecificationExecutor<CopySubscriberDetails> {}
