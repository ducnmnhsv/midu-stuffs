package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyTradingRegister;
import com.difisoft.nhsv.admin.repository.CopyTradingRegisterRepository;
import com.difisoft.nhsv.admin.service.CopyTradingRegisterService;
import com.difisoft.nhsv.admin.service.dto.CopyTradingRegisterDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyTradingRegisterMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link CopyTradingRegister}.
 */
@Service
@Transactional
public class CopyTradingRegisterServiceImpl implements CopyTradingRegisterService {
    private final Logger log = LoggerFactory.getLogger(CopyTradingRegisterServiceImpl.class);
    private final CopyTradingRegisterRepository copyTradingRegisterRepository;
    private final CopyTradingRegisterMapper copyTradingRegisterMapper;

    public CopyTradingRegisterServiceImpl(
        CopyTradingRegisterRepository copyTradingRegisterRepository,
        CopyTradingRegisterMapper copyTradingRegisterMapper
    ) {
        this.copyTradingRegisterRepository = copyTradingRegisterRepository;
        this.copyTradingRegisterMapper = copyTradingRegisterMapper;
    }

    @Override
    public CopyTradingRegisterDTO save(CopyTradingRegisterDTO copyTradingRegisterDTO) {
        log.debug("Request to save CopyTradingRegister : {}", copyTradingRegisterDTO);
        CopyTradingRegister copyTradingRegister = copyTradingRegisterMapper.toEntity(copyTradingRegisterDTO);
        copyTradingRegister = copyTradingRegisterRepository.save(copyTradingRegister);
        return copyTradingRegisterMapper.toDto(copyTradingRegister);
    }

    @Override
    public CopyTradingRegisterDTO update(CopyTradingRegisterDTO copyTradingRegisterDTO) {
        log.debug("Request to update CopyTradingRegister : {}", copyTradingRegisterDTO);
        CopyTradingRegister copyTradingRegister = copyTradingRegisterMapper.toEntity(copyTradingRegisterDTO);
        copyTradingRegister = copyTradingRegisterRepository.save(copyTradingRegister);
        return copyTradingRegisterMapper.toDto(copyTradingRegister);
    }

    @Override
    public Optional<CopyTradingRegisterDTO> partialUpdate(CopyTradingRegisterDTO copyTradingRegisterDTO) {
        log.debug("Request to partially update CopyTradingRegister : {}", copyTradingRegisterDTO);
        return copyTradingRegisterRepository
            .findById(copyTradingRegisterDTO.getId())
            .map(existingCopyTradingRegister -> {
                copyTradingRegisterMapper.partialUpdate(existingCopyTradingRegister, copyTradingRegisterDTO);
                return existingCopyTradingRegister;
            })
            .map(copyTradingRegisterRepository::save)
            .map(copyTradingRegisterMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopyTradingRegisterDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopyTradingRegisters");
        return copyTradingRegisterRepository.findAll(pageable).map(copyTradingRegisterMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyTradingRegisterDTO> findOne(Long id) {
        log.debug("Request to get CopyTradingRegister : {}", id);
        return copyTradingRegisterRepository.findById(id).map(copyTradingRegisterMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopyTradingRegister : {}", id);
        copyTradingRegisterRepository.deleteById(id);
    }
}
