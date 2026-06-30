package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory;
import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailHistoryRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailHistoryService;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailHistoryDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioDetailHistoryMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CopyPortfolioDetailHistory}.
 */
@Service
@Transactional
public class CopyPortfolioDetailHistoryServiceImpl implements CopyPortfolioDetailHistoryService {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioDetailHistoryServiceImpl.class);

    private final CopyPortfolioDetailHistoryRepository copyPortfolioDetailHistoryRepository;

    private final CopyPortfolioDetailHistoryMapper copyPortfolioDetailHistoryMapper;

    public CopyPortfolioDetailHistoryServiceImpl(
        CopyPortfolioDetailHistoryRepository copyPortfolioDetailHistoryRepository,
        CopyPortfolioDetailHistoryMapper copyPortfolioDetailHistoryMapper
    ) {
        this.copyPortfolioDetailHistoryRepository = copyPortfolioDetailHistoryRepository;
        this.copyPortfolioDetailHistoryMapper = copyPortfolioDetailHistoryMapper;
    }

    @Override
    public CopyPortfolioDetailHistoryDTO save(CopyPortfolioDetailHistoryDTO copyPortfolioDetailHistoryDTO) {
        log.debug("Request to save CopyPortfolioDetailHistory : {}", copyPortfolioDetailHistoryDTO);
        CopyPortfolioDetailHistory copyPortfolioDetailHistory = copyPortfolioDetailHistoryMapper.toEntity(copyPortfolioDetailHistoryDTO);
        copyPortfolioDetailHistory = copyPortfolioDetailHistoryRepository.save(copyPortfolioDetailHistory);
        return copyPortfolioDetailHistoryMapper.toDto(copyPortfolioDetailHistory);
    }

    @Override
    public CopyPortfolioDetailHistoryDTO update(CopyPortfolioDetailHistoryDTO copyPortfolioDetailHistoryDTO) {
        log.debug("Request to update CopyPortfolioDetailHistory : {}", copyPortfolioDetailHistoryDTO);
        CopyPortfolioDetailHistory copyPortfolioDetailHistory = copyPortfolioDetailHistoryMapper.toEntity(copyPortfolioDetailHistoryDTO);
        copyPortfolioDetailHistory = copyPortfolioDetailHistoryRepository.save(copyPortfolioDetailHistory);
        return copyPortfolioDetailHistoryMapper.toDto(copyPortfolioDetailHistory);
    }

    @Override
    public Optional<CopyPortfolioDetailHistoryDTO> partialUpdate(CopyPortfolioDetailHistoryDTO copyPortfolioDetailHistoryDTO) {
        log.debug("Request to partially update CopyPortfolioDetailHistory : {}", copyPortfolioDetailHistoryDTO);

        return copyPortfolioDetailHistoryRepository
            .findById(copyPortfolioDetailHistoryDTO.getId())
            .map(existingCopyPortfolioDetailHistory -> {
                copyPortfolioDetailHistoryMapper.partialUpdate(existingCopyPortfolioDetailHistory, copyPortfolioDetailHistoryDTO);

                return existingCopyPortfolioDetailHistory;
            })
            .map(copyPortfolioDetailHistoryRepository::save)
            .map(copyPortfolioDetailHistoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopyPortfolioDetailHistoryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopyPortfolioDetailHistories");
        return copyPortfolioDetailHistoryRepository.findAll(pageable).map(copyPortfolioDetailHistoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyPortfolioDetailHistoryDTO> findOne(Long id) {
        log.debug("Request to get CopyPortfolioDetailHistory : {}", id);
        return copyPortfolioDetailHistoryRepository.findById(id).map(copyPortfolioDetailHistoryMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopyPortfolioDetailHistory : {}", id);
        copyPortfolioDetailHistoryRepository.deleteById(id);
    }
}
