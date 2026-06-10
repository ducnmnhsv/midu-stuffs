import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteTransferStockHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteTransferStockHistoryResponseData[];
}

export interface ILotteTransferStockHistoryResponseData {
  proc_dt: string;
  seq_no: string;
  acnt_no_r: string;
  sub_no_r: string;
  cust_nm_r: string;
  stk_cd: string;
  qty: string;
  sb_lmt_qty: string;
  cnte: string;
  next: string;
}
