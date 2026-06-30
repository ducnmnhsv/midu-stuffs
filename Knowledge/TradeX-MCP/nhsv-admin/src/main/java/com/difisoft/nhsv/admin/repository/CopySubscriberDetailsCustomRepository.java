package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopySubscriberDetails;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
public interface CopySubscriberDetailsCustomRepository extends CopySubscriberDetailsRepository {
    @Query("SELECT csd FROM CopySubscriberDetails csd WHERE csd.copySubscriberId.id = :subscriberId")
    Optional<CopySubscriberDetails> findBySubscriberId(@Param("subscriberId") Long subscriberId);
}
