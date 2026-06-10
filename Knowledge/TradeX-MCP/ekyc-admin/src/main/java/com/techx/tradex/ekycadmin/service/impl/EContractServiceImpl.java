package com.techx.tradex.ekycadmin.service.impl;

import com.techx.tradex.ekycadmin.domain.EContract;
import com.techx.tradex.ekycadmin.repository.EContractRepository;
import com.techx.tradex.ekycadmin.service.EContractService;
import com.techx.tradex.ekycadmin.service.dto.EContractDTO;
import com.techx.tradex.ekycadmin.service.mapper.EContractMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link EContract}.
 */
@Service
@Transactional
public class EContractServiceImpl implements EContractService {

    private final Logger log = LoggerFactory.getLogger(EContractServiceImpl.class);

    private final EContractRepository eContractRepository;

    private final EContractMapper eContractMapper;

    public EContractServiceImpl(EContractRepository eContractRepository, EContractMapper eContractMapper) {
        this.eContractRepository = eContractRepository;
        this.eContractMapper = eContractMapper;
    }

    @Override
    public EContractDTO save(EContractDTO eContractDTO) {
        log.debug("Request to save EContract : {}", eContractDTO);
        EContract eContract = eContractMapper.toEntity(eContractDTO);
        eContract = eContractRepository.save(eContract);
        return eContractMapper.toDto(eContract);
    }

    @Override
    public Optional<EContractDTO> partialUpdate(EContractDTO eContractDTO) {
        log.debug("Request to partially update EContract : {}", eContractDTO);

        return eContractRepository
                .findById(eContractDTO.getId())
                .map(
                        existingEContract -> {
                            eContractMapper.partialUpdate(existingEContract, eContractDTO);
                            return existingEContract;
                        }
                )
                .map(eContractRepository::save)
                .map(eContractMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EContractDTO> findAll(Pageable pageable) {
        log.debug("Request to get all EContracts");
        return eContractRepository.findAll(pageable).map(eContractMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EContractDTO> findOne(Long id) {
        log.debug("Request to get EContract : {}", id);
        return eContractRepository.findById(id).map(eContractMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete EContract : {}", id);
        eContractRepository.deleteById(id);
    }
}
