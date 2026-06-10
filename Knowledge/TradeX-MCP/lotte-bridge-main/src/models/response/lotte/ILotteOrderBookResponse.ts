import { OrderModifyCancelType, OrderStatus, OrderType, SellBuyTypeLotte } from '../../../constants/enum';
import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteOrderBookResponse extends ILotteCommonResponse {
  data_list: ILotteOrderBookResponseData[];
}

export interface ILotteOrderBookResponseData {
  ord_tm: string;
  ord_no: string;
  orgord_no: string;
  stk_cd: string;
  modcan_tp: OrderModifyCancelType;
  sellbuy_tp: SellBuyTypeLotte;
  ord_tp: OrderType;
  ord_qty: string;
  mth_qty: string;
  unmth_qty: string;
  ord_pri: string;
  mth_pri: string;
  ord_stat: OrderStatus;
  mod_stt: string;
  next_date: string;
  next_key: string;
}
