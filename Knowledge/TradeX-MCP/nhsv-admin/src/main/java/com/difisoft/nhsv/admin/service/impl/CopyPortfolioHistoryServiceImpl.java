package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyPortfolioHistory;
import com.difisoft.nhsv.admin.repository.CopyPortfolioHistoryRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioHistoryService;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioHistoryDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioHistoryMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CopyPortfolioHistory}.
 */
@Service
@Transactional
public class CopyPortfolioHistoryServiceImpl implements CopyPortfolioHistoryService {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioHistoryServiceImpl.class);

    private final CopyPortfolioHistoryRepository copyPortfolioHistoryRepository;

    private final CopyPortfolioHistoryMapper copyPortfolioHistoryMapper;

    public CopyPortfolioHistoryServiceImpl(
        CopyPortfolioHistoryRepository copyPortfolioHistoryRepository,
        CopyPortfolioHistoryMapper copyPortfolioHistoryMapper
    ) {
        this.copyPortfolioHistoryRepository = copyPortfolioHistoryRepository;
        this.copyPortfolioHistoryMapper = copyPortfolioHistoryMapper;
    }

    @Override
    public CopyPortfolioHistoryDTO save(CopyPortfolioHistoryDTO copyPortfolioHistoryDTO) {
        log.debug("Request to save CopyPortfolioHistory : {}", copyPortfolioHistoryDTO);
        CopyPortfolioHistory copyPortfolioHistory = copyPortfolioHistoryMapper.toEntity(copyPortfolioHistoryDTO);
        copyPortfolioHistory = copyPortfolioHistoryRepository.save(copyPortfolioHistory);
        return copyPortfolioHistoryMapper.toDto(copyPortfolioHistory);
    }

    @Override
    public CopyPortfolioHistoryDTO update(CopyPortfolioHistoryDTO copyPortfolioHistoryDTO) {
        log.debug("Request to update CopyPortfolioHistory : {}", copyPortfolioHistoryDTO);
        CopyPortfolioHistory copyPortfolioHistory = copyPortfolioHistoryMapper.toEntity(copyPortfolioHistoryDTO);
        copyPortfolioHistory = copyPortfolioHistoryRepository.save(copyPortfolioHistory);
        return copyPortfolioHistoryMapper.toDto(copyPortfolioHistory);
    }

    @Override
    public Optional<CopyPortfolioHistoryDTO> partialUpdate(CopyPortfolioHistoryDTO copyPortfolioHistoryDTO) {
        log.debug("Request to partially update CopyPortfolioHistory : {}", copyPortfolioHistoryDTO);

        return copyPortfolioHistoryRepository
            .findById(copyPortfolioHistoryDTO.getId())
            .map(existingCopyPortfolioHistory -> {
                copyPortfolioHistoryMapper.partialUpdate(existingCopyPortfolioHistory, copyPortfolioHistoryDTO);

                return existingCopyPortfolioHistory;
            })
            .map(copyPortfolioHistoryRepository::save)
            .map(copyPortfolioHistoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopyPortfolioHistoryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopyPortfolioHistories");
        return copyPortfolioHistoryRepository.findAll(pageable).map(copyPortfolioHistoryMapper::toDto);
    }

    public Page<CopyPortfolioHistoryDTO> findAllWithEagerRelationships(Pageable pageable) {
        return copyPortfolioHistoryRepository.findAllWithEagerRelationships(pageable).map(copyPortfolioHistoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyPortfolioHistoryDTO> findOne(Long id) {
        log.debug("Request to get CopyPortfolioHistory : {}", id);
        return copyPortfolioHistoryRepository.findOneWithEagerRelationships(id).map(copyPortfolioHistoryMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopyPortfolioHistory : {}", id);
        copyPortfolioHistoryRepository.deleteById(id);
    }
}
