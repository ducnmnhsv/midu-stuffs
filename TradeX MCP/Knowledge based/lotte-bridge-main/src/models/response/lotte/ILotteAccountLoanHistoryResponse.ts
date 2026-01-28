import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteAccountLoanHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteAccountLoanHistoryResponseData[];
}

export interface ILotteAccountLoanHistoryResponseData {
  lnd_dt: string;
  expr_dt: string;
  stk_cd: string;
  lnd_tp: string;
  lnd_qty: string;
  lnd_amt: string;
  lnd_int_rm: string;
  lnd_rpy_amt: string;
  lnd_rm_amt: string;
  expr_dt_tp: string;
  next: string;
  tot_lnd: string;
}
