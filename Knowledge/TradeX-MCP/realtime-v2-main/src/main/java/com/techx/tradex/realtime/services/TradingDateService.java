package com.techx.tradex.realtime.services;

import com.difisoft.market.common.utils.TradingDate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TradingDateService extends TradingDate {
    @Autowired
    public TradingDateService(ObjectMapper objectMapper, AppConf appConf) {
        super(objectMapper, appConf.getHolidayUrl(), appConf.getHolidayLocalFile());
    }

    @PostConstruct
    public void init() {
        this.start();
    }
}
