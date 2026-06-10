package com.difisoft.marketcollector.model.response;

import lombok.Data;

@Data
public class RefreshNotify {
    private RefreshType type = RefreshType.MARKET_DATA;

    public static RefreshNotify getNotifyGzip() {
        RefreshNotify refreshNotify = new RefreshNotify();
        refreshNotify.setType(RefreshType.MARKET_GZIP);
        return refreshNotify;
    }

    public static RefreshNotify getNotifyMarketData() {
        RefreshNotify refreshNotify = new RefreshNotify();
        refreshNotify.setType(RefreshType.MARKET_DATA);
        return refreshNotify;
    }
}

enum RefreshType {
    MARKET_DATA, MARKET_GZIP
}
