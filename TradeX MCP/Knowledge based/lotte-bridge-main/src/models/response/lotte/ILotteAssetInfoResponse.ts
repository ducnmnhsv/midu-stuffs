import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteAssetInfoResponse extends ILotteCommonResponse {
  data_list: ILotteAssetInfoData[];
}

export interface ILotteAssetInfoData {
  not_total_asset: string;
  total_asset: string;
  gst_dpo: string;
  use_vd: string;
  mgn_loan_amt: string;
  cash_total: string;
  psbamt: string;
  cash_not_hold: string;
  col_loan_amt: string;
  cash_hold: string;
  buy_wait_amt: string;
  able_qty_value: string;
  rgt_dpo: string;
  loan_int: string;
  mrgn_shrt_amt: string;
  able_amt_margin_rto_value: string;
  wait_qty_for_buy_2: string;
  lim_qty_value: string;
  rgt_cash: string;
  loan_amt_t1: string;
  vd_amt_t1: string;
  loan_sum_amt_t1: string;
  mrgn_now_mntn_rt: string;
  notyet_pia_loan_amt: string;
  stk_amt_total: string;
  loan_total: string;
  lack_amt_settl: string;
  stk_margin_rto_total: string;
  mrgn_mntn_rt: string;
  mrgn_mntn_rt_ta: string;
  all_prf_net: string;
  not_yet_math_amout: string;
  depo_amt: string;
  rgt_sbstamt: string;
  delay_rate: string;
  delay_amt_margin_rto_value: string;
  lnd_real: string;
  mrgn_now_mntn_bp_rt: string;
  acnt_nonmth_sbst_amt: string;
  cdr_sbst_acnt: string;
  nonmth_sbst_value: string;
  cdr_bp_acnt: string;
  waiting_dpo_cw: string;
  waiting_amt_sbst_rto_value: string;
  rgt_sbst_value: string;
  delay_amt_sbst_rto_value: string;
  waiting_amt_margin_rto_value: string;
  able_amt_sbst_rto_value: string;
  mgn_loan_amt_src: string;
  mn_can_withdraw: string;
  CMRb: string;
  unused_grt_mn: string;
}
