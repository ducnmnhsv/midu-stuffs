import {
  MarketType,
  MarketTypeAdvanced,
  MatchType,
  SellBuyTypeAdvanced,
  SellBuyTypeLotte,
  SortType,
} from '../../../constants/enum';

export interface ILotteHistoryOrderRequest {
  acnt_no: string;
  sub_no: string;
  from_dt: string;
  to_dt: string;
  sellbuy_type: SellBuyTypeLotte;
  stock_code: string;
  srt_type: SortType;
  mth_type: MatchType;
  mkt_type: MarketType;
  next_date: string;
  next_key: string;
  row_count: string;
}

export interface ILotteHistoryOrderAdvancedRequest {
  acnt_no: string;
  stk_cd: string;
  mkt_tp: MarketTypeAdvanced;
  sell_buy_tp: SellBuyTypeAdvanced;
  sub_no: string;
  next_key: string;
  row_count: string;
}
