package com.difisoft.marketcollector.constants;

import com.difisoft.market.model.constant.MarketTypeEnum;

public enum LotteExchange {
    HNX, HSX, UPCOM;


    public com.difisoft.market.model.constant.MarketTypeEnum toMarketType() {
        if (this == LotteExchange.HNX) return com.difisoft.market.model.constant.MarketTypeEnum.HNX;
        if (this == LotteExchange.UPCOM) return com.difisoft.market.model.constant.MarketTypeEnum.UPCOM;
        return MarketTypeEnum.HOSE;
    }
}
