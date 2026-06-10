import { OrderStatus, OrderType, SellBuyTypeLotte } from '../../../constants/enum';
import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteTodayUnmatchOrderResponse extends ILotteCommonResponse {
  data_list: ILotteTodayUnmatchOrderResponseData[];
}

export interface ILotteTodayUnmatchOrderResponseData {
  ord_tm: string;
  stk_cd: string;
  sellbuy_tp: SellBuyTypeLotte;
  ord_qty: string;
  ord_pri: string;
  unmth_qty: string;
  ord_stat: OrderStatus;
  mkttrd_tp: string;
  bnh_cd: string;
  ord_no: string;
  org_ordno: string;
  usr_id: string;
  mdm_tp: string;
  ord_tp: OrderType;
  bnk_cd: string;
  bnk_nm: string;
}
