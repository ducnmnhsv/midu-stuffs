package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopySubscriberHistory;

import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface CopySubscriberHistoryCustomRepository extends CopySubscriberHistoryRepository {

    Optional<CopySubscriberHistory> findByAccountNumberAndSubNumberAndUserName(String accountNumber, String subNumber, String username);

}
