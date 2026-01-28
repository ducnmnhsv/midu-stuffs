import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteUpcomingRightResponse extends ILotteCommonResponse {
  data_list: ILotteUpcomingRightResponseData[];
}

export interface ILotteUpcomingRightResponseData {
  listItems: ILotteUpcomingRightItem[];
}

export interface ILotteUpcomingRightItem {
  stk_cd: string;
  rgt_tp: string;
  rgt_std_dt: string;
  qty: string;
  inq_dt: string;
  flotq_amt: string;
  flotq_dt: string;
  asn_amt: string;
  divi_pay_dt: string;
  next_key: string;
}
