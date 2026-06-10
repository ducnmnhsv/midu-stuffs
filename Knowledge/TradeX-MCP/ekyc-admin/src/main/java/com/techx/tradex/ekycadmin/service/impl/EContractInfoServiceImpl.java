package com.techx.tradex.ekycadmin.service.impl;

import com.techx.tradex.ekycadmin.domain.EContractInfo;
import com.techx.tradex.ekycadmin.repository.EContractInfoRepository;
import com.techx.tradex.ekycadmin.service.EContractInfoService;
import com.techx.tradex.ekycadmin.service.dto.EContractInfoDTO;
import com.techx.tradex.ekycadmin.service.mapper.EContractInfoMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link EContractInfo}.
 */
@Service
@Transactional
public class EContractInfoServiceImpl implements EContractInfoService {

    private final Logger log = LoggerFactory.getLogger(EContractInfoServiceImpl.class);

    private final EContractInfoRepository eContractInfoRepository;

    private final EContractInfoMapper eContractInfoMapper;

    public EContractInfoServiceImpl(EContractInfoRepository eContractInfoRepository, EContractInfoMapper eContractInfoMapper) {
        this.eContractInfoRepository = eContractInfoRepository;
        this.eContractInfoMapper = eContractInfoMapper;
    }

    @Override
    public EContractInfoDTO save(EContractInfoDTO eContractInfoDTO) {
        log.debug("Request to save EContractInfo : {}", eContractInfoDTO);
        EContractInfo eContractInfo = eContractInfoMapper.toEntity(eContractInfoDTO);
        eContractInfo = eContractInfoRepository.save(eContractInfo);
        return eContractInfoMapper.toDto(eContractInfo);
    }

    @Override
    public Optional<EContractInfoDTO> partialUpdate(EContractInfoDTO eContractInfoDTO) {
        log.debug("Request to partially update EContractInfo : {}", eContractInfoDTO);

        return eContractInfoRepository
            .findById(eContractInfoDTO.getId())
            .map(
                existingEContractInfo -> {
                    eContractInfoMapper.partialUpdate(existingEContractInfo, eContractInfoDTO);
                    return existingEContractInfo;
                }
            )
            .map(eContractInfoRepository::save)
            .map(eContractInfoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EContractInfoDTO> findAll(Pageable pageable) {
        log.debug("Request to get all EContractInfos");
        return eContractInfoRepository.findAll(pageable).map(eContractInfoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EContractInfoDTO> findOne(Long id) {
        log.debug("Request to get EContractInfo : {}", id);
        return eContractInfoRepository.findById(id).map(eContractInfoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete EContractInfo : {}", id);
        eContractInfoRepository.deleteById(id);
    }
}
