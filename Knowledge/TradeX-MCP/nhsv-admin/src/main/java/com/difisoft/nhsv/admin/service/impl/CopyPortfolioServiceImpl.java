package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyPortfolio;
import com.difisoft.nhsv.admin.repository.CopyPortfolioRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioService;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CopyPortfolio}.
 */
@Service
@Transactional
public class CopyPortfolioServiceImpl implements CopyPortfolioService {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioServiceImpl.class);

    private final CopyPortfolioRepository copyPortfolioRepository;

    private final CopyPortfolioMapper copyPortfolioMapper;

    public CopyPortfolioServiceImpl(CopyPortfolioRepository copyPortfolioRepository, CopyPortfolioMapper copyPortfolioMapper) {
        this.copyPortfolioRepository = copyPortfolioRepository;
        this.copyPortfolioMapper = copyPortfolioMapper;
    }

    @Override
    public CopyPortfolioDTO save(CopyPortfolioDTO copyPortfolioDTO) {
        log.debug("Request to save CopyPortfolio : {}", copyPortfolioDTO);
        CopyPortfolio copyPortfolio = copyPortfolioMapper.toEntity(copyPortfolioDTO);
        copyPortfolio = copyPortfolioRepository.save(copyPortfolio);
        return copyPortfolioMapper.toDto(copyPortfolio);
    }

    @Override
    public CopyPortfolioDTO update(CopyPortfolioDTO copyPortfolioDTO) {
        log.debug("Request to update CopyPortfolio : {}", copyPortfolioDTO);
        CopyPortfolio copyPortfolio = copyPortfolioMapper.toEntity(copyPortfolioDTO);
        copyPortfolio = copyPortfolioRepository.save(copyPortfolio);
        return copyPortfolioMapper.toDto(copyPortfolio);
    }

    @Override
    public Optional<CopyPortfolioDTO> partialUpdate(CopyPortfolioDTO copyPortfolioDTO) {
        log.debug("Request to partially update CopyPortfolio : {}", copyPortfolioDTO);

        return copyPortfolioRepository
            .findById(copyPortfolioDTO.getId())
            .map(existingCopyPortfolio -> {
                copyPortfolioMapper.partialUpdate(existingCopyPortfolio, copyPortfolioDTO);

                return existingCopyPortfolio;
            })
            .map(copyPortfolioRepository::save)
            .map(copyPortfolioMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopyPortfolioDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopyPortfolios");
        return copyPortfolioRepository.findAll(pageable).map(copyPortfolioMapper::toDto);
    }

    public Page<CopyPortfolioDTO> findAllWithEagerRelationships(Pageable pageable) {
        return copyPortfolioRepository.findAllWithEagerRelationships(pageable).map(copyPortfolioMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyPortfolioDTO> findOne(Long id) {
        log.debug("Request to get CopyPortfolio : {}", id);
        return copyPortfolioRepository.findOneWithEagerRelationships(id).map(copyPortfolioMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopyPortfolio : {}", id);
        copyPortfolioRepository.deleteById(id);
    }
}
