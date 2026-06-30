package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLoss;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderProfitLossRepository;
import com.difisoft.nhsv.admin.service.CopyMarketLeaderProfitLossService;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderProfitLossDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyMarketLeaderProfitLossMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CopyMarketLeaderProfitLoss}.
 */
@Service
@Transactional
public class CopyMarketLeaderProfitLossServiceImpl implements CopyMarketLeaderProfitLossService {

    private final Logger log = LoggerFactory.getLogger(CopyMarketLeaderProfitLossServiceImpl.class);

    private final CopyMarketLeaderProfitLossRepository copyMarketLeaderProfitLossRepository;

    private final CopyMarketLeaderProfitLossMapper copyMarketLeaderProfitLossMapper;

    public CopyMarketLeaderProfitLossServiceImpl(
        CopyMarketLeaderProfitLossRepository copyMarketLeaderProfitLossRepository,
        CopyMarketLeaderProfitLossMapper copyMarketLeaderProfitLossMapper
    ) {
        this.copyMarketLeaderProfitLossRepository = copyMarketLeaderProfitLossRepository;
        this.copyMarketLeaderProfitLossMapper = copyMarketLeaderProfitLossMapper;
    }

    @Override
    public CopyMarketLeaderProfitLossDTO save(CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossDTO) {
        log.debug("Request to save CopyMarketLeaderProfitLoss : {}", copyMarketLeaderProfitLossDTO);
        CopyMarketLeaderProfitLoss copyMarketLeaderProfitLoss = copyMarketLeaderProfitLossMapper.toEntity(copyMarketLeaderProfitLossDTO);
        copyMarketLeaderProfitLoss = copyMarketLeaderProfitLossRepository.save(copyMarketLeaderProfitLoss);
        return copyMarketLeaderProfitLossMapper.toDto(copyMarketLeaderProfitLoss);
    }

    @Override
    public CopyMarketLeaderProfitLossDTO update(CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossDTO) {
        log.debug("Request to update CopyMarketLeaderProfitLoss : {}", copyMarketLeaderProfitLossDTO);
        CopyMarketLeaderProfitLoss copyMarketLeaderProfitLoss = copyMarketLeaderProfitLossMapper.toEntity(copyMarketLeaderProfitLossDTO);
        copyMarketLeaderProfitLoss = copyMarketLeaderProfitLossRepository.save(copyMarketLeaderProfitLoss);
        return copyMarketLeaderProfitLossMapper.toDto(copyMarketLeaderProfitLoss);
    }

    @Override
    public Optional<CopyMarketLeaderProfitLossDTO> partialUpdate(CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossDTO) {
        log.debug("Request to partially update CopyMarketLeaderProfitLoss : {}", copyMarketLeaderProfitLossDTO);

        return copyMarketLeaderProfitLossRepository
            .findById(copyMarketLeaderProfitLossDTO.getId())
            .map(existingCopyMarketLeaderProfitLoss -> {
                copyMarketLeaderProfitLossMapper.partialUpdate(existingCopyMarketLeaderProfitLoss, copyMarketLeaderProfitLossDTO);

                return existingCopyMarketLeaderProfitLoss;
            })
            .map(copyMarketLeaderProfitLossRepository::save)
            .map(copyMarketLeaderProfitLossMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopyMarketLeaderProfitLossDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopyMarketLeaderProfitLosses");
        return copyMarketLeaderProfitLossRepository.findAll(pageable).map(copyMarketLeaderProfitLossMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyMarketLeaderProfitLossDTO> findOne(Long id) {
        log.debug("Request to get CopyMarketLeaderProfitLoss : {}", id);
        return copyMarketLeaderProfitLossRepository.findById(id).map(copyMarketLeaderProfitLossMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopyMarketLeaderProfitLoss : {}", id);
        copyMarketLeaderProfitLossRepository.deleteById(id);
    }
}
