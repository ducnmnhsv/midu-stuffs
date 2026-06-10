import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteWithdrawHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteWithdrawHistoryResponseData[];
}

export interface ILotteWithdrawHistoryResponseData {
  date: string;
  tr_seq: string;
  seq: string;
  amount: string;
  bank: string;
  bank_account: string;
  remark: string;
  cancel_yn: string;
  approved_by: string;
  approved_at: string;
  nextkey: string;
}
