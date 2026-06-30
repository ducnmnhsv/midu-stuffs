package com.difisoft.nhsv.admin.repository;

import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.nhsv.admin.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.constants.MarketTypeEnum;
import com.techx.tradex.common.constants.RedisDataTypeEnum;
import com.techx.tradex.common.constants.SymbolTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class RedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public static final Logger log = LoggerFactory.getLogger(RedisRepository.class);


    @Autowired
    public RedisRepository(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public SymbolInfo findStock(String stockCode) {
        if (StringUtils.isEmpty(StringUtils.trim(stockCode))) {
            throw new GeneralException(Constants.STOCK_CODE_REQUIRED);
        }
        SymbolInfo symbolInfo = null;
        try {
            Object data = redisTemplate.opsForHash().get(Constants.REDIS_KEY_SYMBOL_INFO, stockCode);
            symbolInfo = stringToObject(String.valueOf(data), SymbolInfo.class);
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (symbolInfo == null) {
                log.info("There was an error when retrieving stock information from Redis.\n" +
                    "To prevent the system from interruption, the current price will be ZERO");
                symbolInfo = new SymbolInfo();
                symbolInfo.setReferencePrice(BigDecimal.ZERO.doubleValue());
            }
        }

        return symbolInfo;
    }

    public List<SymbolInfo> findByIndexStock(String indexCode) {
        List<SymbolInfo> stockList = findAllSymbol();
        return stockList
            .stream()
            .filter(stock -> SymbolTypeEnum.INDEX.equals(stock.getType()) && stock.getCode().startsWith(indexCode))
            .collect(Collectors.toList());
    }

    public List<SymbolInfo> findBy(MarketTypeEnum marketType, SymbolTypeEnum symbolType) {
        List<SymbolInfo> stockList = findAllSymbol();
        return stockList
            .stream()
            .filter(isStockOnMarket(symbolType, marketType))
            .collect(Collectors.toList());
    }

    private Predicate<SymbolInfo> isStockOnMarket(SymbolTypeEnum symbolType, MarketTypeEnum marketType) {
        return stock -> (marketType.equals(stock.getMarketType())) && (symbolType.equals(stock.getType()));
    }

    public List<SymbolInfo> findAllStock() {
        List<SymbolInfo> stockList = findAllSymbol();
        return stockList
            .stream()
            .filter(stock -> SymbolTypeEnum.STOCK.equals(stock.getType()))
            .collect(Collectors.toList());
    }

    public List<SymbolInfo> findAllSymbol() {
        List<SymbolInfo> symbolInfoList = new ArrayList<>();
        try {
            HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
            List<Object> listSymbolInfoStr = hashOperations.values(Constants.REDIS_KEY_SYMBOL_INFO);
            for (Object data : listSymbolInfoStr) {
                SymbolInfo symbolInfo = stringToObject(String.valueOf(data), SymbolInfo.class);
                symbolInfoList.add(symbolInfo);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return symbolInfoList;
    }

    private <E> E stringToObject(String data, Class<E> clazz) throws IOException {
        if ("null".equals(data)) {
            return null;
        }
        String type = data.substring(0, 1);
        if (type.equals(RedisDataTypeEnum.NULL.getType())) {
            return null;
        }
        String value = data.substring(1);
        if (type.equals(RedisDataTypeEnum.BOOLEAN.getType())) {
            return (E) Boolean.valueOf(value);
        } else if (type.equals(RedisDataTypeEnum.STRING.getType())) {
            return (E) value;
        } else if (type.equals(RedisDataTypeEnum.NUMBER.getType())) {
            return (E) Double.valueOf(value);
        } else {
            return objectMapper.readValue(value, clazz);
        }
    }
}
