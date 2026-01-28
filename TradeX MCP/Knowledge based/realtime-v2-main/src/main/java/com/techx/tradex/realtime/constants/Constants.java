package com.techx.tradex.realtime.constants;

public interface Constants {
    String REDIS_KEY_SYMBOL_INFO = "realtime_mapSymbolInfo";
    String REDIS_KEY_SYMBOL_INFO_ODD_LOT = "realtime_mapSymbolInfoOddLot";
    String REDIS_KEY_SYMBOL_DAILY = "realtime_mapSymbolDaily";
    String REDIS_KEY_FOREIGNER_DAILY = "realtime_mapForeignerDaily";
    String REDIS_KEY_SYMBOL_QUOTE = "realtime_listQuote";
    String REDIS_KEY_SYMBOL_BID_OFFER = "realtime_listBidOffer";
    String REDIS_KEY_SYMBOL_BID_OFFER_ODD_LOT = "realtime_listBidOfferOddLot";
    String REDIS_KEY_SYMBOL_QUOTE_MINUTE = "realtime_listQuoteMinute";
    String REDIS_KEY_DEAL_NOTICE = "realtime_listDealNotice";
    String REDIS_KEY_ADVERTISED = "realtime_listAdvertised";
    String REDIS_KEY_MARKET_STATUS = "realtime_mapMarketStatus";
    Integer DEFAULT_HIGHLIGHT_NUMBER = 1000;
    String DATE_TIME_FORMAT_yyyyMMddHHmmss = "yyyyMMddHHmmss";
    String KAFKA_REQUEST_ASYNC = "KAFKA_REQUEST_ASYNC";
    String PAAVE_STOCK_TOP_WORST_RETURNS_NOTIFICATION_TEMPLATE_NAME = "paave_stock_top_worst_vn_index_returns";
    int DEFAULT_OFFSET = 0;
    String INDEX_NOT_FOUND = "INDEX_NOT_FOUND";
    String VN_INDEX = "VN";

}
