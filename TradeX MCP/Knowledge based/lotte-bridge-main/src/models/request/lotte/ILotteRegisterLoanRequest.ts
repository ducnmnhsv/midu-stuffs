export interface ILotteRegisterLoanRequest {
  hts_user_id: string;
  dept_no1: string;
  acnt_no: string;
  sub_no: string;
  setl_bank_cd: string;
  mth_dt: string; //yyyyMMdd
  setl_dt: string; //yyyyMMdd
  stk_cd: string;
  mrtg_lnd_qty: string;
  mth_amt: string;
  mth_cmsn: string;
  adj_amt: string;
  lnd_abl_amt: string;
  lnd_amt: string;
  lnd_rt: string;
  sb_tax: string;
  cdt_tp: string;
  idno: string;
}
