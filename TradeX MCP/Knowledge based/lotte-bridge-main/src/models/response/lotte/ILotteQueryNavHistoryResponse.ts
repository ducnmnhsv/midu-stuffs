import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteQueryNavHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteQueryNavHistoryResponseData[];
}

export interface ILotteQueryNavHistoryResponseData {
  date: string;
  net_asset_val: string;
  cash_in: number;
  cash_out: number;
  profit_daily: number;
  profit_total: number;
  next_data: number;
}
