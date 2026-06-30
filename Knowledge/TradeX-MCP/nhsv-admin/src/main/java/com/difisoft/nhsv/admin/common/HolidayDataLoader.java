package com.difisoft.nhsv.admin.common;

import com.difisoft.nhsv.admin.domain.Holiday;
import com.difisoft.nhsv.admin.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HolidayDataLoader implements ApplicationRunner {

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private HolidayConfiguration holidayConfiguration;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Holiday> holidays = holidayRepository.findAll();
        holidayConfiguration.set(holidays);
    }
}
