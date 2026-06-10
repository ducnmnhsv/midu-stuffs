import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteSellableResponse extends ILotteCommonResponse {
  data_list: ILotteSellableResponseData[];
}

export interface ILotteSellableResponseData {
  stk_code: string;
  own_qty: string;
  ppd_sell_mth_qty: string;
  ppd_buy_mth_qty: string;
  pd_sell_mth_qty: string;
  pd_buy_mth_qty: string;
  td_sell_mth_qty: string;
  td_buy_mth_qty: string;
  sell_psb_qty: string;
  prof_rt: string;
  next_key: string;
}
