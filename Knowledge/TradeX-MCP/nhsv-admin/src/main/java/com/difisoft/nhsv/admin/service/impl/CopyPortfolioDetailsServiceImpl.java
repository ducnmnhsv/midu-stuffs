package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyPortfolioDetails;
import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailsRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailsService;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailsDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioDetailsMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CopyPortfolioDetails}.
 */
@Service
@Transactional
public class CopyPortfolioDetailsServiceImpl implements CopyPortfolioDetailsService {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioDetailsServiceImpl.class);

    private final CopyPortfolioDetailsRepository copyPortfolioDetailsRepository;

    private final CopyPortfolioDetailsMapper copyPortfolioDetailsMapper;

    public CopyPortfolioDetailsServiceImpl(
        CopyPortfolioDetailsRepository copyPortfolioDetailsRepository,
        CopyPortfolioDetailsMapper copyPortfolioDetailsMapper
    ) {
        this.copyPortfolioDetailsRepository = copyPortfolioDetailsRepository;
        this.copyPortfolioDetailsMapper = copyPortfolioDetailsMapper;
    }

    @Override
    public CopyPortfolioDetailsDTO save(CopyPortfolioDetailsDTO copyPortfolioDetailsDTO) {
        log.debug("Request to save CopyPortfolioDetails : {}", copyPortfolioDetailsDTO);
        CopyPortfolioDetails copyPortfolioDetails = copyPortfolioDetailsMapper.toEntity(copyPortfolioDetailsDTO);
        copyPortfolioDetails = copyPortfolioDetailsRepository.save(copyPortfolioDetails);
        return copyPortfolioDetailsMapper.toDto(copyPortfolioDetails);
    }

    @Override
    public CopyPortfolioDetailsDTO update(CopyPortfolioDetailsDTO copyPortfolioDetailsDTO) {
        log.debug("Request to update CopyPortfolioDetails : {}", copyPortfolioDetailsDTO);
        CopyPortfolioDetails copyPortfolioDetails = copyPortfolioDetailsMapper.toEntity(copyPortfolioDetailsDTO);
        copyPortfolioDetails = copyPortfolioDetailsRepository.save(copyPortfolioDetails);
        return copyPortfolioDetailsMapper.toDto(copyPortfolioDetails);
    }

    @Override
    public Optional<CopyPortfolioDetailsDTO> partialUpdate(CopyPortfolioDetailsDTO copyPortfolioDetailsDTO) {
        log.debug("Request to partially update CopyPortfolioDetails : {}", copyPortfolioDetailsDTO);

        return copyPortfolioDetailsRepository
            .findById(copyPortfolioDetailsDTO.getId())
            .map(existingCopyPortfolioDetails -> {
                copyPortfolioDetailsMapper.partialUpdate(existingCopyPortfolioDetails, copyPortfolioDetailsDTO);

                return existingCopyPortfolioDetails;
            })
            .map(copyPortfolioDetailsRepository::save)
            .map(copyPortfolioDetailsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopyPortfolioDetailsDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopyPortfolioDetails");
        return copyPortfolioDetailsRepository.findAll(pageable).map(copyPortfolioDetailsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyPortfolioDetailsDTO> findOne(Long id) {
        log.debug("Request to get CopyPortfolioDetails : {}", id);
        return copyPortfolioDetailsRepository.findById(id).map(copyPortfolioDetailsMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopyPortfolioDetails : {}", id);
        copyPortfolioDetailsRepository.deleteById(id);
    }
}
