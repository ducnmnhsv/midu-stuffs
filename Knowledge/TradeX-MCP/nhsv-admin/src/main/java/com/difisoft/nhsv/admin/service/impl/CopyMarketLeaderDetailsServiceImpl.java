package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderDetailsRepository;
import com.difisoft.nhsv.admin.service.CopyMarketLeaderDetailsService;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyMarketLeaderDetailsMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CopyMarketLeaderDetails}.
 */
@Service
@Transactional
public class CopyMarketLeaderDetailsServiceImpl implements CopyMarketLeaderDetailsService {

    private final Logger log = LoggerFactory.getLogger(CopyMarketLeaderDetailsServiceImpl.class);

    private final CopyMarketLeaderDetailsRepository copyMarketLeaderDetailsRepository;

    private final CopyMarketLeaderDetailsMapper copyMarketLeaderDetailsMapper;

    public CopyMarketLeaderDetailsServiceImpl(
        CopyMarketLeaderDetailsRepository copyMarketLeaderDetailsRepository,
        CopyMarketLeaderDetailsMapper copyMarketLeaderDetailsMapper
    ) {
        this.copyMarketLeaderDetailsRepository = copyMarketLeaderDetailsRepository;
        this.copyMarketLeaderDetailsMapper = copyMarketLeaderDetailsMapper;
    }

    @Override
    public CopyMarketLeaderDetailsDTO save(CopyMarketLeaderDetailsDTO copyMarketLeaderDetailsDTO) {
        log.debug("Request to save CopyMarketLeaderDetails : {}", copyMarketLeaderDetailsDTO);
        CopyMarketLeaderDetails copyMarketLeaderDetails = copyMarketLeaderDetailsMapper.toEntity(copyMarketLeaderDetailsDTO);
        copyMarketLeaderDetails = copyMarketLeaderDetailsRepository.save(copyMarketLeaderDetails);
        return copyMarketLeaderDetailsMapper.toDto(copyMarketLeaderDetails);
    }

    @Override
    public CopyMarketLeaderDetailsDTO update(CopyMarketLeaderDetailsDTO copyMarketLeaderDetailsDTO) {
        log.debug("Request to update CopyMarketLeaderDetails : {}", copyMarketLeaderDetailsDTO);
        CopyMarketLeaderDetails copyMarketLeaderDetails = copyMarketLeaderDetailsMapper.toEntity(copyMarketLeaderDetailsDTO);
        copyMarketLeaderDetails = copyMarketLeaderDetailsRepository.save(copyMarketLeaderDetails);
        return copyMarketLeaderDetailsMapper.toDto(copyMarketLeaderDetails);
    }

    @Override
    public Optional<CopyMarketLeaderDetailsDTO> partialUpdate(CopyMarketLeaderDetailsDTO copyMarketLeaderDetailsDTO) {
        log.debug("Request to partially update CopyMarketLeaderDetails : {}", copyMarketLeaderDetailsDTO);

        return copyMarketLeaderDetailsRepository
            .findById(copyMarketLeaderDetailsDTO.getId())
            .map(existingCopyMarketLeaderDetails -> {
                copyMarketLeaderDetailsMapper.partialUpdate(existingCopyMarketLeaderDetails, copyMarketLeaderDetailsDTO);

                return existingCopyMarketLeaderDetails;
            })
            .map(copyMarketLeaderDetailsRepository::save)
            .map(copyMarketLeaderDetailsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopyMarketLeaderDetailsDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopyMarketLeaderDetails");
        return copyMarketLeaderDetailsRepository.findAll(pageable).map(copyMarketLeaderDetailsMapper::toDto);
    }

    public Page<CopyMarketLeaderDetailsDTO> findAllWithEagerRelationships(Pageable pageable) {
        return copyMarketLeaderDetailsRepository.findAllWithEagerRelationships(pageable).map(copyMarketLeaderDetailsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyMarketLeaderDetailsDTO> findOne(Long id) {
        log.debug("Request to get CopyMarketLeaderDetails : {}", id);
        return copyMarketLeaderDetailsRepository.findOneWithEagerRelationships(id).map(copyMarketLeaderDetailsMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopyMarketLeaderDetails : {}", id);
        copyMarketLeaderDetailsRepository.deleteById(id);
    }
}
