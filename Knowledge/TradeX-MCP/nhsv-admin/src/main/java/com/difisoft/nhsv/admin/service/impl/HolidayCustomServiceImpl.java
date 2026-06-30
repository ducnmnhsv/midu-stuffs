package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.common.HolidayConfiguration;
import com.difisoft.nhsv.admin.domain.Holiday;
import com.difisoft.nhsv.admin.repository.HolidayRepository;
import com.difisoft.nhsv.admin.service.mapper.HolidayMapper;
import com.difisoft.nhsv.admin.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service("holidayCustomService")
@Primary
@Transactional
public class HolidayCustomServiceImpl extends HolidayServiceImpl {

    private final HolidayConfiguration holidayConfiguration;

    @Autowired
    public HolidayCustomServiceImpl(HolidayRepository holidayRepository, HolidayMapper holidayMapper, HolidayConfiguration holidayConfiguration) {
        super(holidayRepository, holidayMapper);
        this.holidayConfiguration = holidayConfiguration;
    }

    public Boolean isHoliday(LocalDate date) {

        AtomicBoolean temp = new AtomicBoolean(false);

        List<Holiday> holiday = holidayConfiguration.get();
        holiday.forEach(h -> {
            if (!date.isBefore(DateTimeUtil.toLocalDate(Date.from(h.getStartDate().toInstant())))
                && !date.isAfter(DateTimeUtil.toLocalDate(Date.from(h.getEndDate().toInstant())))) {
                temp.set(true);
            }
        });

        return temp.get();
    }

}
