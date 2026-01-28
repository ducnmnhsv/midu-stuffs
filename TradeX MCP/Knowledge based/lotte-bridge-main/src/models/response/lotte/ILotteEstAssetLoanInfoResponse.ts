import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteEstAssetLoanInfoResponse extends ILotteCommonResponse {
  data_list: ILotteEstAssetLoanInfoResponseData[];
}

export interface ILotteEstAssetLoanInfoResponseData {
  sbst_rt: string;
  tot_loan_real: string;
  cmr: string;
  short_amt_lmr: string;
}
