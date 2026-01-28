import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteAccountStkBalanceResponse extends ILotteCommonResponse {
  data_list: ILotteAccountStkBalanceResponseData[];
}

export interface ILotteAccountStkBalanceResponseData {
  next_data: string;
  listItems: ILotteAccountStkBalanceResponseDataItem[];
}

export interface ILotteAccountStkBalanceResponseDataItem {
  stk_cd: string;
  own_qty: string;
  mrtg_lnd_qty: string;
  sell_able_qty: string;
  qty: string;
  buy_uv: string;
  cur_pri: string;
  book_amt: string;
  eval_amt: string;
  eval_per: string;
  td_buy_mth_qty: string;
  pd_buy_mth_qty: string;
  ppd_buy_mth_qty: string;
  td_sell_mth_qty: string;
  pd_sell_mth_qty: string;
  ppd_sell_mth_qty: string;
  pnl_amount: string;
}
