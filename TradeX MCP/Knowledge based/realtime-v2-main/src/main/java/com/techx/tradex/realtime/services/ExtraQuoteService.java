package com.techx.tradex.realtime.services;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.utils.ConvertUtils;
import com.difisoft.market.model.v2.db.ForeignerDaily;
import com.difisoft.market.model.v2.db.SymbolDaily;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.utils.DefaultUtils;
import com.techx.tradex.realtime.model.request.ExtraQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ExtraQuoteService {
    private static final Logger log = LoggerFactory.getLogger(ExtraQuoteService.class);

    private final MarketRedisDao marketRedisDao;
    private final CacheService cacheService;

    public ExtraQuoteService(
            MarketRedisDao marketRedisDao,
            CacheService cacheService) {
        this.marketRedisDao = marketRedisDao;
        this.cacheService = cacheService;
    }

    public void updateExtraQuote(ExtraQuote extraQuote) {
        if (cacheService.isEnableAutoData()) {
            SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(extraQuote.getCode());
            ForeignerDaily foreignerDaily = cacheService.getMapForeignerDaily().get(extraQuote.getCode());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            if (symbolInfo != null) {
                ConvertUtils.updateByExtraQuote(symbolInfo, extraQuote);
                marketRedisDao.setSymbolInfo(symbolInfo);
            }
            if (foreignerDaily != null) {
                ConvertUtils.updateByExtraQuote(foreignerDaily, extraQuote);
                try {
                    foreignerDaily.setDate(extraQuote.getDate() == null
                            ? DefaultUtils.DATE_FORMAT().parse(simpleDateFormat.format(new Date()))
                            : DefaultUtils.DATE_FORMAT().parse(simpleDateFormat.format(extraQuote.getDate())));
                } catch (ParseException e) {
                    log.error("Error when parse date: {} {}", extraQuote.getDate(), e);
                    foreignerDaily.setDate(new Date());
                }
                marketRedisDao.setForeignerDaily(foreignerDaily);
            }
            this.upsertSymbolDaily(extraQuote, symbolInfo);
        } else {
            log.warn("Ignore Info/Daily by quote because of not enableAutoData");
        }
    }

    public void upsertSymbolDaily(ExtraQuote extraQuote, SymbolInfo info) {
        SymbolDaily symbolDaily = cacheService.getMapSymbolDaily().get(extraQuote.getCode());
        if (symbolDaily == null) {
            if (info != null) {
                symbolDaily = new SymbolDaily();
                ConvertUtils.fromSymbolInfo(symbolDaily, info);
            } else {
                return;
            }
        }
        ConvertUtils.updateByExtraQuote(symbolDaily, extraQuote);
        marketRedisDao.setSymbolDaily(symbolDaily);
        cacheService.getMapSymbolDaily().put(extraQuote.getCode(), symbolDaily);
    }

}
