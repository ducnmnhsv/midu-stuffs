package com.techx.tradex.realtime.services;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.SymbolDailyRepository;
import com.difisoft.market.common.repository.SymbolInfoRollerRepository;
import com.difisoft.market.model.common.HighLowYearItem;
import com.difisoft.market.model.v2.db.SymbolDaily;
import com.difisoft.market.model.v2.db.SymbolInfoRoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SymbolInfoRollerService {
    private static final Logger log = LoggerFactory.getLogger(SymbolInfoRollerService.class);

    private SymbolDailyRepository symbolDailyRepository;
    private SymbolInfoRollerRepository symbolInfoRollerRepository;
    private MarketRedisDao redisService;

    public SymbolInfoRollerService(
            SymbolDailyRepository symbolDailyRepository,
            SymbolInfoRollerRepository symbolInfoRollerRepository,
            MarketRedisDao redisService
    ) {
        this.symbolDailyRepository = symbolDailyRepository;
        this.symbolInfoRollerRepository = symbolInfoRollerRepository;
        this.redisService = redisService;
    }

    public void rollerData() {
        log.info("start calculate rolling data");
        Map<String, SymbolInfoRoller> map = new HashMap<>();
        try {
            this.calculateHighLowYear(map);
        } catch (Exception e) {
            log.error("fail to calculate high low year", e);
        }
        this.symbolInfoRollerRepository.deleteAll();
        this.symbolInfoRollerRepository.saveAll(map.values());
    }

    public boolean updateHighLowYear(Object data, RequestContext<Object> ctx) {
        log.info("Start update high low year");
        Map<String, SymbolInfoRoller> map = new HashMap<>();
        this.calculateHighLowYear(map);
        log.info("Finish calculate high low year");
        this.redisService.getAllSymbolInfo().forEach(it -> {
            SymbolInfoRoller roller = map.get(it.getCode());
            if (roller != null) {
                it.setHighLowYearData(roller.getHighLowYearData());
                this.redisService.setSymbolInfo(it);
            }
        });
        log.info("Finish update high low year");
        return true;
    }

    public void calculateHighLowYear(Map<String, SymbolInfoRoller> map) {
        log.info("start calculate high low year");
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, date.get(Calendar.YEAR) - 1);
        date.set(Calendar.HOUR, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        Date startDate = date.getTime();
        log.info("high low year: date: {}", startDate);
        this.symbolDailyRepository.findByDateGreaterThanOrderByCodeAscDateDesc(startDate).forEach(it -> {
            SymbolInfoRoller current = map.get(it.getCode());
            if (current == null) {
                current = new SymbolInfoRoller();
                current.setCode(it.getCode());
                map.put(it.getCode(), current);
            }
            if (current.getHighLowYearData() == null || current.getHighLowYearData().isEmpty()) {
                current.setHighLowYearData(new ArrayList<>());
                current.getHighLowYearData().add(this.create(it));
            } else {
                HighLowYearItem highLowYearItem = current.getHighLowYearData().get(0);
                if (it.getHigh() != null && it.getHigh() > 0 && (highLowYearItem.getHighPrice() == null || it.getHigh() > highLowYearItem.getHighPrice())) {
                    highLowYearItem.setHighPrice(it.getHigh());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    highLowYearItem.setDateOfHighPrice(simpleDateFormat.format(it.getDate()));
                }
                if (it.getLow() != null && it.getLow() > 0 && (highLowYearItem.getLowPrice() == null || highLowYearItem.getLowPrice() <= 0 || it.getLow() < highLowYearItem.getLowPrice())) {
                    highLowYearItem.setLowPrice(it.getLow());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    highLowYearItem.setDateOfLowPrice(simpleDateFormat.format(it.getDate()));
                }
            }
        });
        log.info("finish calculate high low year");
    }

    private HighLowYearItem create(SymbolDaily it) {
        HighLowYearItem highLowYearItem = new HighLowYearItem();
        highLowYearItem.setHighPrice(it.getHigh());
        highLowYearItem.setLowPrice(it.getLow());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        highLowYearItem.setDateOfHighPrice(simpleDateFormat.format(it.getDate()));
        highLowYearItem.setDateOfLowPrice(simpleDateFormat.format(it.getDate()));
        return highLowYearItem;
    }
}
