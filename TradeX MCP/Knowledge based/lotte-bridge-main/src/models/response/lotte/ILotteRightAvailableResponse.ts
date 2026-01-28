import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteRightAvailableResponse extends ILotteCommonResponse {
  data_list: ILotteRightAvailableResponseData[];
}

export interface ILotteRightAvailableResponseData {
  stk_cd: string;
  stk_nm: string;
  seq: number;
  base_date: string;
  proc_nm: string;
  start_dt: string;
  end_dt: string;
  right_price: number;
  avai_stk_qty: number;
  cnte: string;
}
