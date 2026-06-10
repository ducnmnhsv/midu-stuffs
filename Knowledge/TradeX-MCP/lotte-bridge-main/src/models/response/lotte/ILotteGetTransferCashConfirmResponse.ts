import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteGetTransferCashConfirmResponse extends ILotteCommonResponse {
  data_list: ILotteGetTransferCashConfirmResponseData[];
}

export interface ILotteGetTransferCashConfirmResponseData {
  trans_sign: string;
  std_dt: string;
  seq_no: string;
  trd_amt: string;
  inamt_acnt_no: string;
  inamt_sub_no: string;
  mdm_tp: string;
  cncl_yn: string;
  cnte: string;
  next: string;
}
