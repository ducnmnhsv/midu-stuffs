import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteLoanHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteLoanHistoryResponseData[];
}

export interface ILotteLoanHistoryResponseData {
  lnd_dt: string;
  mth_dt: string;
  stk_cd: string;
  mrtg_lnd_qty: string;
  mth_amt: string;
  lnd_amt: string;
  lnd_rpy_amt: string;
  lnd_rm_amt: string;
  lnd_proc_stat: string;
  lnd_bank_cd: string;
  lnd_bank_nm: string;
  next_data: string;
}
