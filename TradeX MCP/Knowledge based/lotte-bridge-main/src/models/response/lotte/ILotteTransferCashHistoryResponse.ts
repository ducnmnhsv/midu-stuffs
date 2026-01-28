import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteTransferCashHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteTransferCashHistoryResponseData[];
}

export interface ILotteTransferCashHistoryResponseData {
  date: string;
  from_seq: string;
  from_account: string;
  from_sub: string;
  from_name: string;
  amount: string;
  to_account: string;
  to_sub: string;
  to_name: string;
  remark: string;
  channel: string;
  cancel_yn: string;
  next_key: string;
}
