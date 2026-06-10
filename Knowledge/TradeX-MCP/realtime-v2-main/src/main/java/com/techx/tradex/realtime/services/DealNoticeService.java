package com.techx.tradex.realtime.services;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.utils.ConvertUtils;
import com.difisoft.market.model.v2.db.DealNotice;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.utils.DefaultUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DealNoticeService {
    private static final Logger log = LoggerFactory.getLogger(DealNoticeService.class);

    private MarketRedisDao marketRedisDao;
    private CacheService cacheService;

    @Autowired
    public DealNoticeService(MarketRedisDao marketRedisDao, CacheService cacheService) {
        this.marketRedisDao = marketRedisDao;
        this.cacheService = cacheService;
    }

    public void updateDealNotice(DealNotice dealNotice) {
        SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(dealNotice.getCode());
        if (symbolInfo == null) {
            log.info("not found symbolInfo {} -> create new", dealNotice.getCode());
            symbolInfo = new SymbolInfo();
            symbolInfo.setCode(dealNotice.getCode());
            symbolInfo.setPtTradingValue(0.0);
            symbolInfo.setPtTradingVolume(0L);
            symbolInfo.setMarketType(dealNotice.getMarketType());
            cacheService.getMapSymbolInfo().put(symbolInfo.getCode(), symbolInfo);
        }
        log.info("symbolInfo: {}", symbolInfo);
        if (symbolInfo.getPtTradingVolume() != null && dealNotice.getPtVolume() < symbolInfo.getPtTradingVolume()) {
            log.warn("ptTradingVolume is less than currently: {}", dealNotice);
        }

        log.info("total ptTradingValue/ptTradingVolume before update: {}/{}", symbolInfo.getPtTradingValue(), symbolInfo.getPtTradingVolume());

        // Check confirmNumber in redis
        if (dealNotice.getConfirmNumber() != null) {
            Set<String> dealNotices = marketRedisDao.getAllDealNotice(dealNotice.getCode()).stream().map(
                    DealNotice::getConfirmNumber).collect(Collectors.toSet());
            if (dealNotices.contains(dealNotice.getConfirmNumber())) {
                log.info("confirmNumber {} is existed", dealNotice.getConfirmNumber());
                return;
            }
        }

        ConvertUtils.updateByDealNotice(symbolInfo, dealNotice);
        marketRedisDao.setSymbolInfo(symbolInfo);

        Date currentDate = new Date();
        dealNotice.setCreatedAt(currentDate);
        dealNotice.setUpdatedAt(currentDate);

        String todayStr = DefaultUtils.DATE_FORMAT().format(currentDate);
        try {
            currentDate = DefaultUtils.DATETIME_FORMAT().parse(todayStr + dealNotice.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dealNotice.setId(dealNotice.getCode() + "_" + System.currentTimeMillis());
        dealNotice.setDate(currentDate);

        marketRedisDao.addDealNotice(dealNotice);
    }
}
