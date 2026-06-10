export interface ILotteOrderConfirmRequest {
  hts_user_id: string;
  acnt_no: string;
  sub_no: string;
  ord_strs: string;
}

export interface ILotteOrderConfirmHistoryRequest {
  acnt_no: string;
  sub_no: string;
  from_date: string;
  to_date: string;
  accp_tp: string;
  mkt_trd_tp: string;
  sell_buy_tp: string;
  mdm_tp: string;
  stk_cd: string;
  crrt_cncl_tp: string;
  next_key: string;
  row_count: string;
}
