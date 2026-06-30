package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopySubscriber;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the CopySubscriber entity.
 */
@Repository
@Primary
public interface CopySubscriberCustomRepository extends CopySubscriberRepository {

    @Query(value = "select cs from CopySubscriber cs inner join User u on cs.mlUserId.id = u.id where cs.mlUserId.id = :mlUserId order by cs.id")
    Page<CopySubscriber> findAllByMlUserId(@Param("mlUserId") Long mlUserId, Pageable pageable);

    Optional<CopySubscriber> findByAccountNumberAndSubNumberAndUserName(String accountNumber, String subNumber, String username);
}
