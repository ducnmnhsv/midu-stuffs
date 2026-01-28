import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteRightDetailResponse extends ILotteCommonResponse {
  data_list: ILotteRightDetailResponseData[];
}

export interface ILotteRightDetailResponseData {
  outamt_trd_no: string;
  sbst_st_dt: string;
  sbst_lst_dt: string;
  own_qty: number;
  rgt_iss_pri: number;
  asn_qty: number;
  cons_sbst_able_amt: number;
  waiting_qty: number;
  cancel_qty: number;
  inq_qty: number;
  rgt_proc_stat: number;
  proc_nm: string;
}
