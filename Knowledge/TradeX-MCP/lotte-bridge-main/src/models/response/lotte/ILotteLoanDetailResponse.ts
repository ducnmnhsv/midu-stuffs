import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteLoanDetailResponse extends ILotteCommonResponse {
  data_list: ILotteLoanDetailResponseData[];
}

export interface ILotteLoanDetailResponseData {
  mth_dt: string;
  setl_dt: string;
  stk_cd: string;
  sb_qty: string;
  sb_amt: string;
  sb_cmsn: string;
  adj_amt: string;
  lnd_abl_amt: string;
  sb_tax: string;
  bank_cd: string;
  bank_nm: string;
  cdt_tp: string;
  next_data: string;
}
