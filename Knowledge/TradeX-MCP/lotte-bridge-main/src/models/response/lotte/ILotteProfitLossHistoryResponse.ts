import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteProfitLossHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteProfitLossHistoryResponseData[];
}

export interface ILotteProfitLossHistoryResponseData {
  tot_adj_amt: string;
  tot_cost_amt: string;
  tot_pl_amt: string;
  tot_pl_rt: string;
  next_data: string;
  listItems: ILotteProfitLossHistoryResponseDataItem[];
}

export interface ILotteProfitLossHistoryResponseDataItem {
  acnt_no: string;
  sub_no: string;
  mth_dt: string;
  stk_cd: string;
  sb_qty: string;
  cost_pri: string;
  sb_pri: string;
  tot_fee_tax: string;
  adj_amt: string;
  cost_amt: string;
  pl_amt: string;
  pl_rt: string;
}
