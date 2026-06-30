package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.CopySubscriberDetails;

import java.util.Optional;

public interface CopySubscriberDetailsCustomService extends CopySubscriberDetailsService {
    CopySubscriberDetails save(CopySubscriberDetails entity);

    Optional<CopySubscriberDetails> findBySubscriberId(Long subscriberId);
}
