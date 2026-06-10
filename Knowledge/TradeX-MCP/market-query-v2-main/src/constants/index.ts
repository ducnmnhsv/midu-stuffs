/* eslint no-unused-vars: 0 */
export const MARKET_TIMEZONE = 7;
export const DEFAULT_DAILY_UPDATE = 3000;
export const DEFAULT_TOP_FOREIGNER_TRADING = 10;
export const DEFAULT_PAGE_SIZE = 100;
export const DEFAULT_DAILY_FETCH_COUNT = 300;
export const DEFAULT_CHART_FETCH_COUNT = 1000;
export const INVALID_PARAMETER = 'INVALID_PARAMETER';
export const DEFAULT_CEILING_SEQUENCE = Number.MAX_SAFE_INTEGER;
export const DEFAULT_FROM_TIME = '19701010000000';
export const DEFAULT_DAILY_FROM_DATE = '1970101';
export const DEFAULT_OFFSET = 0;
export const DEFAULT_BATCH_PROCESS_ADJUSTED_PRICE = 100;
export const POWER_STOCK_SORT_THRESHOLD = 100000000;
export const DEFAULT_QUERY_DAILY_RETURN_DAYS = 20;
export const DEFAULT_FLOOR_DATE = new Date('1970-01-01');
export const MONGO_MAX_SAFE_ARRAY_SIZE = 99999999;
export const MAX_WATCHLIST = 20;
export const MAX_SYMBOL_WATCHLIST = 50;

export const COLLECTIONS_NAME = {
  BID_OFFER: 'c_bid_offer',
  SYMBOL: 'c_symbol',
  SYMBOL_INFO: 'c_symbol_info',
  SYMBOL_DAILY: 'c_symbol_daily',
  SYMBOL_PREVIOUS: 'c_symbol_previous',
  SYMBOL_WEEKLY: 'c_symbol_weekly',
  SYMBOL_MONTHLY: 'c_symbol_monthly',
  SYMBOL_QUOTE: 'c_symbol_quote',
  SYMBOL_QUOTE_BACKUP: 'c_symbol_quote_backup',
  SYMBOL_QUOTE_HISTORY: 'c_symbol_quote_history',
  SYMBOL_QUOTE_MINUTE: 'c_symbol_quote_minute',
  SYMBOL_QUOTE_MINUTE_BACKUP: 'c_symbol_quote_minute_backup',
  MARKET_SESSION_STATUS: 'c_market_session_status',
  ETF_NAV_DAILY: 'c_etf_nav_daily',
  ETF_INDEX_DAILY: 'c_etf_index_daily',
  SYMBOL_FOREIGNER_DAILY: 'c_foreigner_daily',
  ADVERTISE_DATA: 'c_advertise',
  DEAL_NOTICE_DATA: 'c_deal_notice',
  INDEX_STOCK_LIST: 'c_index_stock_list',
  DIVIDEND: 'c_dividend',
  FUTURES_DAILY_LIST_HISTORY: 'c_futures_daily_list_history',
  CHART: 'c_chart',
  TOP_AI_RATING: 't_top_ai_rating',
  SYMBOL_INFO_EXTEND: 'c_symbol_info_extend',
  MARKET_INFO: 'c_market_info',
  SYMBOL_HISTORY_EVENTS: 't_event_history',
  WATCH_LIST: 't_watch_list',
};
export const PERIOD_TYPE = {
  DAILY: 'DAILY',
  WEEKLY: 'WEEKLY',
  MONTHLY: 'MONTHLY',
  SIX_MONTH: 'SIX_MONTH',
};

export const MARKET_INFO_FIELD = {
  LAST_TRADING_DATE: 'LAST_TRADING_DATE',
  CURRENT_DIVIDEND_EVENT: 'CURRENT_DIVIDEND_EVENT',
};

export enum MarketSessionStatusEnum {
  ATO = 'ATO',
  LO = 'LO',
  INTERMISSION = 'INTERMISSION',
  ATC = 'ATC',
  PLO = 'PLO',
  PUT_THROUGH = 'PUT_THROUGH',
  CLOSED = 'CLOSED',
  RUNOFF = 'RUNOFF',
  BUY_IN = 'BUY_IN',
}

export enum MarketTypeEnum {
  ALL = 'ALL',
  HNX = 'HNX',
  HOSE = 'HOSE',
  UPCOM = 'UPCOM',
}

export enum MarketSessionTypeEnum {
  EQUITY = 'EQUITY',
  DERIVATIVES = 'DERIVATIVES',
}

export enum StockRankingTradeSortTypeEnum {
  TURNOVER_RATE = 'turnoverRate',
  TRADING_VOLUME = 'tradingVolume',
  TRADING_VALUE = 'tradingValue',
}

export enum UpDownTypeEnum {
  UP = 'UP',
  DOWN = 'DOWN',
}

export enum ForeignerRankingTypeEnum {
  BUY = 'BUY',
  SELL = 'SELL',
}

export enum SecuritiesTypeEnum {
  INDEX = 'INDEX',
  STOCK = 'STOCK',
  FUTURES = 'FUTURES',
  CW = 'CW',
}

export enum MatchByTypeEnum {
  CEILING = 'CEILING',
  FLOOR = 'FLOOR',
}

export enum IndexTypeEnum {
  FOREIGN = 'FOREIGN',
  DOMESTIC = 'DOMESTIC',
}

export enum BaseCodeSecuritiesTypeEnum {
  INDEX = 'INDEX',
  BOND = 'BOND',
}

export const DIVIDEND_INFO = {
  BONUS_SHARE: 'BONUS_SHARE',
  RIGHTS_ISSUE: 'RIGHTS_ISSUE',
  DIVIDEND: 'DIVIDEND',
  PAR_VALUE_VN: 10000,
};

export const SUPPORTED_RESOLUTION = ['1', '3', '5', '10', '15', '30', '60', '1D', '1W', '1M', '6M'];
export enum StatusResponseEnum {
  OK = 'ok',
  ERROR = 'error',
  NO_DATA = 'no_data',
}
export const RESOLUTION_MINUTE: string[] = ['1', '3', '5', '10', '15', '30', '60'];

export const RESOLUTION_PERIOD = {
  DAILY: ['1D', 'D'],
  WEEKLY: ['1W', 'W'],
  MONTHLY: ['1M', 'M'],
  SIX_MONTHLY: ['6M'],
};

export enum TopSortTypeEnum {
  TRADING_VOLUME = 'TRADING_VOLUME',
  TRADING_VALUE = 'TRADING_VALUE',
  CHANGE = 'CHANGE',
  RATE = 'RATE',
  POWER = 'POWER',
}

export const FOREIGNER_SUMMARY_SORT_TYPE = {
  CODE: 'CODE',
  NET_VALUE: 'NET_VALUE',
  NET_VOLUME: 'NET_VOLUME',
};

export const MARKET_INDEX_ENUM = {
  HOSE: 'VN',
  HNX: 'HNX',
  UPCOM: 'UPCOM',
};

export const PRICE_BOARD_CATEGORY = {
  HOSE_STOCK: 'HOSE_STOCK',
  HNX_STOCK: 'HNX_STOCK',
  UPCOM_STOCK: 'UPCOM_STOCK',
  HOSE_CW: 'HOSE_CW',
  HNX_FUTURES: 'HNX_FUURES',
  FAVORITE_LIST: 'FAVORITE_LIST',
  INDEX: 'INDEX',
};

export const COLOR = {
  BLUE: 'BLUE',
};
