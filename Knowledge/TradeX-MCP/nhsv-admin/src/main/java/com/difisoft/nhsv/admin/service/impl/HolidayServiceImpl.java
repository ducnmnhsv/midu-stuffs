package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.domain.Holiday;
import com.difisoft.nhsv.admin.repository.HolidayRepository;
import com.difisoft.nhsv.admin.service.HolidayService;
import com.difisoft.nhsv.admin.service.dto.HolidayDTO;
import com.difisoft.nhsv.admin.service.mapper.HolidayMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Holiday}.
 */
@Service
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private final Logger log = LoggerFactory.getLogger(HolidayServiceImpl.class);

    private final HolidayRepository holidayRepository;

    private final HolidayMapper holidayMapper;

    public HolidayServiceImpl(HolidayRepository holidayRepository, HolidayMapper holidayMapper) {
        this.holidayRepository = holidayRepository;
        this.holidayMapper = holidayMapper;
    }

    @Override
    public HolidayDTO save(HolidayDTO holidayDTO) {
        log.debug("Request to save Holiday : {}", holidayDTO);
        Holiday holiday = holidayMapper.toEntity(holidayDTO);
        holiday = holidayRepository.save(holiday);
        return holidayMapper.toDto(holiday);
    }

    @Override
    public HolidayDTO update(HolidayDTO holidayDTO) {
        log.debug("Request to update Holiday : {}", holidayDTO);
        Holiday holiday = holidayMapper.toEntity(holidayDTO);
        holiday = holidayRepository.save(holiday);
        return holidayMapper.toDto(holiday);
    }

    @Override
    public Optional<HolidayDTO> partialUpdate(HolidayDTO holidayDTO) {
        log.debug("Request to partially update Holiday : {}", holidayDTO);

        return holidayRepository
            .findById(holidayDTO.getId())
            .map(existingHoliday -> {
                holidayMapper.partialUpdate(existingHoliday, holidayDTO);

                return existingHoliday;
            })
            .map(holidayRepository::save)
            .map(holidayMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HolidayDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Holidays");
        return holidayRepository.findAll(pageable).map(holidayMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HolidayDTO> findOne(Long id) {
        log.debug("Request to get Holiday : {}", id);
        return holidayRepository.findById(id).map(holidayMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Holiday : {}", id);
        holidayRepository.deleteById(id);
    }
}
