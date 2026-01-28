package com.techx.tradex.realtime.services;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.utils.ConvertUtils;
import com.difisoft.market.model.v2.db.BidOffer;
import com.difisoft.market.model.v2.db.BidOfferOddLot;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.utils.DefaultUtils;
import com.techx.tradex.realtime.configurations.AppConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BidOfferService {
    private static final Logger log = LoggerFactory.getLogger(BidOfferService.class);

    private final AppConf appConf;
    private final MarketRedisDao marketRedisDao;
    private final CacheService cacheService;

    @Autowired
    public BidOfferService(AppConf appConf, MarketRedisDao marketRedisDao, CacheService cacheService) {
        this.appConf = appConf;
        this.marketRedisDao = marketRedisDao;
        this.cacheService = cacheService;
    }

    public void updateBidOffer(BidOffer bidOffer) {
        Date currentDate = new Date();
        bidOffer.setCreatedAt(currentDate);
        bidOffer.setUpdatedAt(currentDate);
        if (cacheService.isEnableAutoData()) {
            SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(bidOffer.getCode());
            if (symbolInfo != null) {
                ConvertUtils.updateByBidOffer(symbolInfo, bidOffer);
                marketRedisDao.setSymbolInfo(symbolInfo);
                bidOffer.setSequence(symbolInfo.getBidAskSequence());
            }
        } else {
            log.warn("Ignore Info/Daily by bidOffer because of not enableAutoData");
        }
        bidOffer.setId(bidOffer.getCode() + "_" + DefaultUtils.DATETIME_FORMAT().format(new Date()) + "_" + bidOffer.getSequence());
        if (appConf.isEnableSaveBidOffer()) {
            marketRedisDao.addBidOffer(bidOffer);
        }
    }

    public void updateBidOfferOddLot(BidOfferOddLot bidOfferOddLot) {
        Date currentDate = new Date();
        bidOfferOddLot.setCreatedAt(currentDate);
        bidOfferOddLot.setUpdatedAt(currentDate);
        if (cacheService.isEnableAutoData()) {
            SymbolInfo symbolInfo = cacheService.getMapSymbolInfoOddLot().get(bidOfferOddLot.getCode());
            if(symbolInfo == null) {
                symbolInfo = new SymbolInfo();
                symbolInfo.setCode(bidOfferOddLot.getCode());
                symbolInfo.setBidAskSequence(1L);
            }
            if (symbolInfo != null) {
                ConvertUtils.updateByBidOfferOddLot(symbolInfo, bidOfferOddLot);
                marketRedisDao.setSymbolInfoOddLot(symbolInfo);
                bidOfferOddLot.setSequence(symbolInfo.getBidAskSequence() + 1);
            }
        } else {
            log.warn("Ignore Info/Daily by bidOffer because of not enableAutoData");
        }
        bidOfferOddLot.setId(bidOfferOddLot.getCode() + "_" + DefaultUtils.DATETIME_FORMAT().format(new Date()) + "_" + bidOfferOddLot.getSequence());
        marketRedisDao.addBidOfferOddLot(bidOfferOddLot);
    }
}
