package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopySubscriberDetails;
import com.difisoft.nhsv.admin.repository.CopySubscriberDetailsCustomRepository;
import com.difisoft.nhsv.admin.repository.CopySubscriberDetailsRepository;
import com.difisoft.nhsv.admin.service.CopySubscriberDetailsCustomService;
import com.difisoft.nhsv.admin.service.mapper.CopySubscriberDetailsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Primary
@Slf4j
public class CopySubscriberDetailsCustomServiceImpl extends CopySubscriberDetailsServiceImpl implements CopySubscriberDetailsCustomService {

    private final CopySubscriberDetailsCustomRepository copySubscriberDetailsCustomRepository;

    @Autowired
    public CopySubscriberDetailsCustomServiceImpl(
        CopySubscriberDetailsRepository copySubscriberDetailsRepository
        , CopySubscriberDetailsMapper copySubscriberDetailsMapper
        , CopySubscriberDetailsCustomRepository copySubscriberDetailsCustomRepository
    ) {
        super(copySubscriberDetailsRepository, copySubscriberDetailsMapper);
        this.copySubscriberDetailsCustomRepository = copySubscriberDetailsCustomRepository;
    }

    @Override
    public CopySubscriberDetails save(CopySubscriberDetails entity) {
        return copySubscriberDetailsCustomRepository.save(entity);
    }

    @Override
    public Optional<CopySubscriberDetails> findBySubscriberId(Long subscriberId) {
        return copySubscriberDetailsCustomRepository.findBySubscriberId(subscriberId);
    }
}
