export interface ILotteIndexListRequest {
  key_search?: string;
  mkt_tp: string;
  next_data?: string;
}

export interface ILotteIndexStockListRequest {
  mkt_tp: string;
  idx: string;
}
