import { OrderType, SellBuyType } from '../../../constants/enum';
import { IParam } from '../../request/lotte/ILotteRequest';
import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteOrderConfirmResponse extends ILotteCommonResponse {
  data_list: IParam[];
}

export interface ILotteOrderConfirmHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteOrderConfirmHistoryResponseData[];
}

export interface ILotteOrderConfirmHistoryResponseData {
  acnt_no: string;
  stk_ord_dt: string;
  ord_time: string;
  sell_buy_tp: SellBuyType;
  ord_no: string;
  stk_cd: string;
  ord_qty: string;
  ord_pri: string;
  mth_qty: string;
  mth_pri: string;
  accp_tp: string;
  work_mn: string;
  mdm_tp: string;
  stk_ord_tp: OrderType;
  next_key: string;
}
