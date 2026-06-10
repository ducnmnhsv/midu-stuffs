package com.techx.tradex.realtime.services;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.model.v2.db.Advertised;
import com.difisoft.model.utils.DefaultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
public class AdvertisedService {

    private MarketRedisDao marketRedisDao;

    @Autowired
    public AdvertisedService(MarketRedisDao marketRedisDao) {
        this.marketRedisDao = marketRedisDao;
    }

    public void updateAdvertised(Advertised advertised) {
        Date currentDate = new Date();
        advertised.setCreatedAt(currentDate);
        advertised.setUpdatedAt(currentDate);

        String todayStr = DefaultUtils.DATE_FORMAT().format(currentDate);
        try {
            currentDate = DefaultUtils.DATETIME_FORMAT().parse(todayStr + advertised.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        advertised.setId(advertised.getCode() + "_" + System.currentTimeMillis());
        advertised.setDate(currentDate);
        marketRedisDao.addAdvertised(advertised);
    }
}
