import {
  OrderModifyCancelType,
  OrderStatus,
  OrderType,
  SellBuyTypeAdvanced,
  SellBuyTypeLotte,
} from '../../../constants/enum';
import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteHistoryOrderResponse extends ILotteCommonResponse {
  data_list: ILotteHistoryOrderResponseData[];
}

export interface ILotteHistoryOrderResponseData {
  ord_dt: string;
  ord_tm: string;
  stk_cd: string;
  sellbuy_tp: SellBuyTypeLotte;
  ord_tp: OrderType;
  ord_qty: string;
  ord_pri: string;
  mth_qty: string;
  mth_pri: string;
  mth_amt: string;
  unmth_qty: string;
  modcan_tp: OrderModifyCancelType;
  modcan_qty: string;
  ord_stat: OrderStatus;
  acnt_no: string;
  sub_no: string;
  ord_no: string;
  orgord_no: string;
  usr_id: string;
  mdm_tp: string;
  bnk_nm: string;
  next_date: string;
  next_key: string;
}

export interface ILotteHistoryOrderAdvancedResponse extends ILotteCommonResponse {
  data_list: ILotteHistoryOrderAdvancedResponseData[];
}

export interface ILotteHistoryOrderAdvancedResponseData {
  ord_no: string;
  stk_cd: string;
  sell_buy_tp: SellBuyTypeAdvanced;
  stk_ord_tp: OrderType;
  ord_qty: number;
  ord_pri: number;
  ord_frct_dt: string;
  work_nm: string;
  mdm_tp: string;
  accp_tp: string;
  ord_time: string;
  next_data: string;
}
