package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopySubscriber;
import com.difisoft.nhsv.admin.repository.CopySubscriberRepository;
import com.difisoft.nhsv.admin.service.CopySubscriberService;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDTO;
import com.difisoft.nhsv.admin.service.mapper.CopySubscriberMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CopySubscriber}.
 */
@Service
@Transactional
public class CopySubscriberServiceImpl implements CopySubscriberService {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberServiceImpl.class);

    private final CopySubscriberRepository copySubscriberRepository;

    private final CopySubscriberMapper copySubscriberMapper;

    public CopySubscriberServiceImpl(CopySubscriberRepository copySubscriberRepository, CopySubscriberMapper copySubscriberMapper) {
        this.copySubscriberRepository = copySubscriberRepository;
        this.copySubscriberMapper = copySubscriberMapper;
    }

    @Override
    public CopySubscriberDTO save(CopySubscriberDTO copySubscriberDTO) {
        log.debug("Request to save CopySubscriber : {}", copySubscriberDTO);
        CopySubscriber copySubscriber = copySubscriberMapper.toEntity(copySubscriberDTO);
        copySubscriber = copySubscriberRepository.save(copySubscriber);
        return copySubscriberMapper.toDto(copySubscriber);
    }

    @Override
    public CopySubscriberDTO update(CopySubscriberDTO copySubscriberDTO) {
        log.debug("Request to update CopySubscriber : {}", copySubscriberDTO);
        CopySubscriber copySubscriber = copySubscriberMapper.toEntity(copySubscriberDTO);
        copySubscriber = copySubscriberRepository.save(copySubscriber);
        return copySubscriberMapper.toDto(copySubscriber);
    }

    @Override
    public Optional<CopySubscriberDTO> partialUpdate(CopySubscriberDTO copySubscriberDTO) {
        log.debug("Request to partially update CopySubscriber : {}", copySubscriberDTO);

        return copySubscriberRepository
            .findById(copySubscriberDTO.getId())
            .map(existingCopySubscriber -> {
                copySubscriberMapper.partialUpdate(existingCopySubscriber, copySubscriberDTO);

                return existingCopySubscriber;
            })
            .map(copySubscriberRepository::save)
            .map(copySubscriberMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopySubscriberDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopySubscribers");
        return copySubscriberRepository.findAll(pageable).map(copySubscriberMapper::toDto);
    }

    public Page<CopySubscriberDTO> findAllWithEagerRelationships(Pageable pageable) {
        return copySubscriberRepository.findAllWithEagerRelationships(pageable).map(copySubscriberMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopySubscriberDTO> findOne(Long id) {
        log.debug("Request to get CopySubscriber : {}", id);
        return copySubscriberRepository.findOneWithEagerRelationships(id).map(copySubscriberMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopySubscriber : {}", id);
        copySubscriberRepository.deleteById(id);
    }
}
