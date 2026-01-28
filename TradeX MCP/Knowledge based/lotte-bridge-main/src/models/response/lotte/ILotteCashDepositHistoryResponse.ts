import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteCashDepositHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteCashDepositHistoryResponseData[];
}

export interface ILotteCashDepositHistoryResponseData {
  trade_dt?: string;
  seq_no?: string;
  rmrk_nm?: string;
  cash_in?: string;
  cash_out?: string;
  dpo_amt?: string;
  cash_note?: string;
  next_key?: string;
  rmrk_cd?: string;
  bal_begin?: string;
  bal_end: string;
  date_begin?: string;
  buy_amt?: string;
  date_end?: string;
  description?: string;
}
