package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopySubscriberDetails;
import com.difisoft.nhsv.admin.repository.CopySubscriberDetailsRepository;
import com.difisoft.nhsv.admin.service.CopySubscriberDetailsService;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDetailsDTO;
import com.difisoft.nhsv.admin.service.mapper.CopySubscriberDetailsMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CopySubscriberDetails}.
 */
@Service
@Transactional
public class CopySubscriberDetailsServiceImpl implements CopySubscriberDetailsService {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberDetailsServiceImpl.class);

    private final CopySubscriberDetailsRepository copySubscriberDetailsRepository;

    private final CopySubscriberDetailsMapper copySubscriberDetailsMapper;

    public CopySubscriberDetailsServiceImpl(
        CopySubscriberDetailsRepository copySubscriberDetailsRepository,
        CopySubscriberDetailsMapper copySubscriberDetailsMapper
    ) {
        this.copySubscriberDetailsRepository = copySubscriberDetailsRepository;
        this.copySubscriberDetailsMapper = copySubscriberDetailsMapper;
    }

    @Override
    public CopySubscriberDetailsDTO save(CopySubscriberDetailsDTO copySubscriberDetailsDTO) {
        log.debug("Request to save CopySubscriberDetails : {}", copySubscriberDetailsDTO);
        CopySubscriberDetails copySubscriberDetails = copySubscriberDetailsMapper.toEntity(copySubscriberDetailsDTO);
        copySubscriberDetails = copySubscriberDetailsRepository.save(copySubscriberDetails);
        return copySubscriberDetailsMapper.toDto(copySubscriberDetails);
    }

    @Override
    public CopySubscriberDetailsDTO update(CopySubscriberDetailsDTO copySubscriberDetailsDTO) {
        log.debug("Request to update CopySubscriberDetails : {}", copySubscriberDetailsDTO);
        CopySubscriberDetails copySubscriberDetails = copySubscriberDetailsMapper.toEntity(copySubscriberDetailsDTO);
        copySubscriberDetails = copySubscriberDetailsRepository.save(copySubscriberDetails);
        return copySubscriberDetailsMapper.toDto(copySubscriberDetails);
    }

    @Override
    public Optional<CopySubscriberDetailsDTO> partialUpdate(CopySubscriberDetailsDTO copySubscriberDetailsDTO) {
        log.debug("Request to partially update CopySubscriberDetails : {}", copySubscriberDetailsDTO);

        return copySubscriberDetailsRepository
            .findById(copySubscriberDetailsDTO.getId())
            .map(existingCopySubscriberDetails -> {
                copySubscriberDetailsMapper.partialUpdate(existingCopySubscriberDetails, copySubscriberDetailsDTO);

                return existingCopySubscriberDetails;
            })
            .map(copySubscriberDetailsRepository::save)
            .map(copySubscriberDetailsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopySubscriberDetailsDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopySubscriberDetails");
        return copySubscriberDetailsRepository.findAll(pageable).map(copySubscriberDetailsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopySubscriberDetailsDTO> findOne(Long id) {
        log.debug("Request to get CopySubscriberDetails : {}", id);
        return copySubscriberDetailsRepository.findById(id).map(copySubscriberDetailsMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopySubscriberDetails : {}", id);
        copySubscriberDetailsRepository.deleteById(id);
    }
}
