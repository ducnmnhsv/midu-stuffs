package com.difisoft.nhsv.admin.market;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.model.v2.db.SymbolInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public final class StockState {

    private final MarketRedisDao redisRepository;

    public StockState(MarketRedisDao redisRepository) {
        this.redisRepository = redisRepository;
    }

    public SymbolInfo getStockState(String code) {
        return redisRepository.getSymbolInfo(code);
    }

    public List<SymbolInfo> getAllSymbols() {
        return redisRepository.getAllSymbolInfo();
    }
}
