import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteGetLoanConfirmResponse extends ILotteCommonResponse {
  data_list: ILotteGetLoanConfirmResponseData[];
}

export interface ILotteGetLoanConfirmResponseData {
  lnd_sign: string;
  lnd_dt: string;
  mth_dt: string;
  lnd_cntr_no: string;
  stk_cd: string;
  day_cnt: string;
  lnd_cmsn_rt: string;
  lnd_amt: string;
  work_mn: string;
  lnd_bank_cd: string;
  next_data: string;
}
