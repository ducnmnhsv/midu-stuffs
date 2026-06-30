package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLossDetails;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderProfitLossDetailsRepository;
import com.difisoft.nhsv.admin.service.CopyMarketLeaderProfitLossDetailsService;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderProfitLossDetailsDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyMarketLeaderProfitLossDetailsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link CopyMarketLeaderProfitLossDetails}.
 */
@Service
@Transactional
public class CopyMarketLeaderProfitLossDetailsServiceImpl implements CopyMarketLeaderProfitLossDetailsService {

    private final Logger log = LoggerFactory.getLogger(CopyMarketLeaderProfitLossDetailsServiceImpl.class);

    private final CopyMarketLeaderProfitLossDetailsRepository copyMarketLeaderProfitLossDetailsRepository;

    private final CopyMarketLeaderProfitLossDetailsMapper copyMarketLeaderProfitLossDetailsMapper;

    public CopyMarketLeaderProfitLossDetailsServiceImpl(
        CopyMarketLeaderProfitLossDetailsRepository copyMarketLeaderProfitLossDetailsRepository,
        CopyMarketLeaderProfitLossDetailsMapper copyMarketLeaderProfitLossDetailsMapper
    ) {
        this.copyMarketLeaderProfitLossDetailsRepository = copyMarketLeaderProfitLossDetailsRepository;
        this.copyMarketLeaderProfitLossDetailsMapper = copyMarketLeaderProfitLossDetailsMapper;
    }

    @Override
    public CopyMarketLeaderProfitLossDetailsDTO save(CopyMarketLeaderProfitLossDetailsDTO copyMarketLeaderProfitLossDetailsDTO) {
        log.debug("Request to save CopyMarketLeaderProfitLossDetails : {}", copyMarketLeaderProfitLossDetailsDTO);
        CopyMarketLeaderProfitLossDetails copyMarketLeaderProfitLossDetails = copyMarketLeaderProfitLossDetailsMapper.toEntity(
            copyMarketLeaderProfitLossDetailsDTO
        );
        copyMarketLeaderProfitLossDetails = copyMarketLeaderProfitLossDetailsRepository.save(copyMarketLeaderProfitLossDetails);
        return copyMarketLeaderProfitLossDetailsMapper.toDto(copyMarketLeaderProfitLossDetails);
    }

    @Override
    public CopyMarketLeaderProfitLossDetailsDTO update(CopyMarketLeaderProfitLossDetailsDTO copyMarketLeaderProfitLossDetailsDTO) {
        log.debug("Request to update CopyMarketLeaderProfitLossDetails : {}", copyMarketLeaderProfitLossDetailsDTO);
        CopyMarketLeaderProfitLossDetails copyMarketLeaderProfitLossDetails = copyMarketLeaderProfitLossDetailsMapper.toEntity(
            copyMarketLeaderProfitLossDetailsDTO
        );
        copyMarketLeaderProfitLossDetails = copyMarketLeaderProfitLossDetailsRepository.save(copyMarketLeaderProfitLossDetails);
        return copyMarketLeaderProfitLossDetailsMapper.toDto(copyMarketLeaderProfitLossDetails);
    }

    @Override
    public Optional<CopyMarketLeaderProfitLossDetailsDTO> partialUpdate(
        CopyMarketLeaderProfitLossDetailsDTO copyMarketLeaderProfitLossDetailsDTO
    ) {
        log.debug("Request to partially update CopyMarketLeaderProfitLossDetails : {}", copyMarketLeaderProfitLossDetailsDTO);

        return copyMarketLeaderProfitLossDetailsRepository
            .findById(copyMarketLeaderProfitLossDetailsDTO.getId())
            .map(existingCopyMarketLeaderProfitLossDetails -> {
                copyMarketLeaderProfitLossDetailsMapper.partialUpdate(
                    existingCopyMarketLeaderProfitLossDetails,
                    copyMarketLeaderProfitLossDetailsDTO
                );

                return existingCopyMarketLeaderProfitLossDetails;
            })
            .map(copyMarketLeaderProfitLossDetailsRepository::save)
            .map(copyMarketLeaderProfitLossDetailsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopyMarketLeaderProfitLossDetailsDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopyMarketLeaderProfitLossDetails");
        return copyMarketLeaderProfitLossDetailsRepository.findAll(pageable).map(copyMarketLeaderProfitLossDetailsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyMarketLeaderProfitLossDetailsDTO> findOne(Long id) {
        log.debug("Request to get CopyMarketLeaderProfitLossDetails : {}", id);
        return copyMarketLeaderProfitLossDetailsRepository.findById(id).map(copyMarketLeaderProfitLossDetailsMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopyMarketLeaderProfitLossDetails : {}", id);
        copyMarketLeaderProfitLossDetailsRepository.deleteById(id);
    }
}
