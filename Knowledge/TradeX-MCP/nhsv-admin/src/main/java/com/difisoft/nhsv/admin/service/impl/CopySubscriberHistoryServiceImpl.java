package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopySubscriberHistory;
import com.difisoft.nhsv.admin.repository.CopySubscriberHistoryRepository;
import com.difisoft.nhsv.admin.service.CopySubscriberHistoryService;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberHistoryDTO;
import com.difisoft.nhsv.admin.service.mapper.CopySubscriberHistoryMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CopySubscriberHistory}.
 */
@Service
@Transactional
public class CopySubscriberHistoryServiceImpl implements CopySubscriberHistoryService {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberHistoryServiceImpl.class);

    private final CopySubscriberHistoryRepository copySubscriberHistoryRepository;

    private final CopySubscriberHistoryMapper copySubscriberHistoryMapper;

    public CopySubscriberHistoryServiceImpl(
        CopySubscriberHistoryRepository copySubscriberHistoryRepository,
        CopySubscriberHistoryMapper copySubscriberHistoryMapper
    ) {
        this.copySubscriberHistoryRepository = copySubscriberHistoryRepository;
        this.copySubscriberHistoryMapper = copySubscriberHistoryMapper;
    }

    @Override
    public CopySubscriberHistoryDTO save(CopySubscriberHistoryDTO copySubscriberHistoryDTO) {
        log.debug("Request to save CopySubscriberHistory : {}", copySubscriberHistoryDTO);
        CopySubscriberHistory copySubscriberHistory = copySubscriberHistoryMapper.toEntity(copySubscriberHistoryDTO);
        copySubscriberHistory = copySubscriberHistoryRepository.save(copySubscriberHistory);
        return copySubscriberHistoryMapper.toDto(copySubscriberHistory);
    }

    @Override
    public CopySubscriberHistoryDTO update(CopySubscriberHistoryDTO copySubscriberHistoryDTO) {
        log.debug("Request to update CopySubscriberHistory : {}", copySubscriberHistoryDTO);
        CopySubscriberHistory copySubscriberHistory = copySubscriberHistoryMapper.toEntity(copySubscriberHistoryDTO);
        copySubscriberHistory = copySubscriberHistoryRepository.save(copySubscriberHistory);
        return copySubscriberHistoryMapper.toDto(copySubscriberHistory);
    }

    @Override
    public Optional<CopySubscriberHistoryDTO> partialUpdate(CopySubscriberHistoryDTO copySubscriberHistoryDTO) {
        log.debug("Request to partially update CopySubscriberHistory : {}", copySubscriberHistoryDTO);

        return copySubscriberHistoryRepository
            .findById(copySubscriberHistoryDTO.getId())
            .map(existingCopySubscriberHistory -> {
                copySubscriberHistoryMapper.partialUpdate(existingCopySubscriberHistory, copySubscriberHistoryDTO);

                return existingCopySubscriberHistory;
            })
            .map(copySubscriberHistoryRepository::save)
            .map(copySubscriberHistoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopySubscriberHistoryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopySubscriberHistories");
        return copySubscriberHistoryRepository.findAll(pageable).map(copySubscriberHistoryMapper::toDto);
    }

    public Page<CopySubscriberHistoryDTO> findAllWithEagerRelationships(Pageable pageable) {
        return copySubscriberHistoryRepository.findAllWithEagerRelationships(pageable).map(copySubscriberHistoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopySubscriberHistoryDTO> findOne(Long id) {
        log.debug("Request to get CopySubscriberHistory : {}", id);
        return copySubscriberHistoryRepository.findOneWithEagerRelationships(id).map(copySubscriberHistoryMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopySubscriberHistory : {}", id);
        copySubscriberHistoryRepository.deleteById(id);
    }
}
