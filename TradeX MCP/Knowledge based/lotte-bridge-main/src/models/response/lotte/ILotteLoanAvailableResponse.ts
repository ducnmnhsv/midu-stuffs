import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteLoanAvailableResponse extends ILotteCommonResponse {
  data_list: ILotteLoanAvailableResponseData[];
}

export interface ILotteLoanAvailableResponseData {
  mth_dt: string; // yyyyMMDD
  setl_dt: string; // yyyyMMDD
  sb_amt: string;
  sb_cmsn: string;
  sb_tax: string;
  adj_amt: string;
  lnd_prd: string;
  lnd_cmsn_rt: string;
  estm_cmsn: string;
  lnd_abl_amt: string;
  bank_nm: string;
  cdt_nm: string;
  bank_cd: string;
  cdt_tp: string;
  next_data: string;
}
