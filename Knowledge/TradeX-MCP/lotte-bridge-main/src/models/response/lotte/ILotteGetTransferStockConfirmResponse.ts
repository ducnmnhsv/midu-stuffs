import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteGetTransferStockConfirmResponse extends ILotteCommonResponse {
  data_list: ILotteGetTransferStockConfirmResponseData[];
}

export interface ILotteGetTransferStockConfirmResponseData {
  status: string;
  proc_dt: string;
  seq_no: string;
  acnt_no_r: string;
  sub_no_r: string;
  stk_cd: string;
  qty: string;
  sb_lmt_qty: string;
  cnte: string;
  next: string;
}
