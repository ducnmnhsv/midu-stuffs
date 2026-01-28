package com.techx.tradex.realtime.services;

import com.difisoft.market.common.utils.HolidayChecker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class HolidayService extends HolidayChecker {
    @Autowired
    public HolidayService(ObjectMapper objectMapper, AppConf appConf) {
        super(objectMapper, appConf.getHolidayUrl(), appConf.getHolidayLocalFile());
    }

    @PostConstruct
    public void init() {
        this.start(30000L);
    }

    public boolean isHolidayOrWeekend() {
        return this.isWeekend() || this.isHoliday();
    }
}
