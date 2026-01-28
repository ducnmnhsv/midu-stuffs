import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteCashBalanceResponse extends ILotteCommonResponse {
  data_list: ILotteCashBalanceData[];
}

export interface ILotteCashBalanceData {
  dpo: string;
  dpo_block: string;
  outq_dpo_bk: string;
  crd_dpo: string;
  nonrpy_loan_amt: string;
  mth_amt: string;
  sbst_dpo: string;
  sbst_proof: string;
  mgn_lack: string;
  cd_lack: string;
  all_prf: string;
  tot_out_psbamt: string;
}
