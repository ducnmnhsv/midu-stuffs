import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteAccountBalanceRetrieveResponse extends ILotteCommonResponse {
  data_list: ILotteAccountBalanceRetrieveResponseData[];
}

export interface ILotteAccountBalanceRetrieveResponseData {
  d1_tot_dpo: string;
  d2_tot_dpo: string;
  dpo: string;
  tot_book_amt: string;
  presum_dpo: string;
  tot_eval_amt: string;
  tot_eval_prf: string;
  tot_bnf_rt: string;
}
