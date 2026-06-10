import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteBuyableResponse extends ILotteCommonResponse {
  data_list: ILotteBuyableData[];
}

export interface ILotteBuyableData {
  buy_abl_qty: string;
  buying_power: string;
  dpo: string;
  gst_dpo: string;
  buy_abl_amt: string;
  init_asst: string;
  lack_blk_amt: string;
  bfr_total_block: string;
  td_total_porf: string;
  max_loan_amt: string;
}
