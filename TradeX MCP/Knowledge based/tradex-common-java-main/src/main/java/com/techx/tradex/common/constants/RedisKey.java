package com.techx.tradex.common.constants;

public interface RedisKey {
    String MAP_SYMBOL_INFO = "realtime_mapSymbolInfo";
    String MAP_SYMBOL_DAILY = "realtime_mapSymbolDaily";
    String MAP_FOREIGNER_DAILY = "realtime_mapForeignerDaily";
    String LIST_SYMBOL_QUOTE = "realtime_listQuote";
    String LIST_SYMBOL_BID_OFFER = "realtime_listBidOffer";
    String LIST_SYMBOL_QUOTE_MINUTE = "realtime_listQuoteMinute";
    String LIST_DEAL_NOTICE = "realtime_listDealNotice";
    String LIST_ADVERTISED = "realtime_listAdvertised";
    String MAP_MARKET_STATUS = "realtime_mapMarketStatus";
}
