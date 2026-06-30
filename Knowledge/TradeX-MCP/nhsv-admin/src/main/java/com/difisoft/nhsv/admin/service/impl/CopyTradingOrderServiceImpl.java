package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.CopyTradingOrder;
import com.difisoft.nhsv.admin.repository.CopyTradingOrderRepository;
import com.difisoft.nhsv.admin.service.CopyTradingOrderService;
import com.difisoft.nhsv.admin.service.dto.CopyTradingOrderDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyTradingOrderMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CopyTradingOrder}.
 */
@Service
@Transactional
public class CopyTradingOrderServiceImpl implements CopyTradingOrderService {

    private final Logger log = LoggerFactory.getLogger(CopyTradingOrderServiceImpl.class);

    private final CopyTradingOrderRepository copyTradingOrderRepository;

    private final CopyTradingOrderMapper copyTradingOrderMapper;

    public CopyTradingOrderServiceImpl(
        CopyTradingOrderRepository copyTradingOrderRepository,
        CopyTradingOrderMapper copyTradingOrderMapper
    ) {
        this.copyTradingOrderRepository = copyTradingOrderRepository;
        this.copyTradingOrderMapper = copyTradingOrderMapper;
    }

    @Override
    public CopyTradingOrderDTO save(CopyTradingOrderDTO copyTradingOrderDTO) {
        log.debug("Request to save CopyTradingOrder : {}", copyTradingOrderDTO);
        CopyTradingOrder copyTradingOrder = copyTradingOrderMapper.toEntity(copyTradingOrderDTO);
        copyTradingOrder = copyTradingOrderRepository.save(copyTradingOrder);
        return copyTradingOrderMapper.toDto(copyTradingOrder);
    }

    @Override
    public CopyTradingOrderDTO update(CopyTradingOrderDTO copyTradingOrderDTO) {
        log.debug("Request to update CopyTradingOrder : {}", copyTradingOrderDTO);
        CopyTradingOrder copyTradingOrder = copyTradingOrderMapper.toEntity(copyTradingOrderDTO);
        copyTradingOrder = copyTradingOrderRepository.save(copyTradingOrder);
        return copyTradingOrderMapper.toDto(copyTradingOrder);
    }

    @Override
    public Optional<CopyTradingOrderDTO> partialUpdate(CopyTradingOrderDTO copyTradingOrderDTO) {
        log.debug("Request to partially update CopyTradingOrder : {}", copyTradingOrderDTO);

        return copyTradingOrderRepository
            .findById(copyTradingOrderDTO.getId())
            .map(existingCopyTradingOrder -> {
                copyTradingOrderMapper.partialUpdate(existingCopyTradingOrder, copyTradingOrderDTO);

                return existingCopyTradingOrder;
            })
            .map(copyTradingOrderRepository::save)
            .map(copyTradingOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CopyTradingOrderDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CopyTradingOrders");
        return copyTradingOrderRepository.findAll(pageable).map(copyTradingOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyTradingOrderDTO> findOne(Long id) {
        log.debug("Request to get CopyTradingOrder : {}", id);
        return copyTradingOrderRepository.findById(id).map(copyTradingOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CopyTradingOrder : {}", id);
        copyTradingOrderRepository.deleteById(id);
    }
}
