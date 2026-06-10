package com.techx.tradex.realtime.utils;

import com.difisoft.market.model.v2.db.SymbolInfo;

import java.math.BigDecimal;

public class MarketUtil {

    public static Double getCurrentPrice(SymbolInfo symbolInfo) {
        if (symbolInfo == null) return null;
        if (symbolInfo.getLast() != null && Math.abs(symbolInfo.getLast()) > 0.0000001) {
            return symbolInfo.getLast();
        }
        return symbolInfo.getReferencePrice();
    }

    public static BigDecimal getBigCurrentPrice(SymbolInfo symbolInfo) {
        Double price = getCurrentPrice(symbolInfo);
        return price == null ? BigDecimal.ZERO : BigDecimal.valueOf(price);
    }

    public static boolean validPrice(Double price) {
        return price != null && Math.abs(price) > 0.0000001;
    }

    public static int compareToZero(Double price) {
        if (price == null) return 0;
        return BigDecimal.ZERO.compareTo(BigDecimal.valueOf(price));
    }
}
