export interface ILotteCancelOrderNormalRequest {
  sub_no: string;
  ord_no: string;
  acnt_no: string;
  bank_cd: string;
  hts_user_id: string;
  hts_user_nm: string;
  cli_ip_addr: string;
  cli_mac_addr: string;
  idno: string;
  lang_code: string;
  mdm_tp: string;
}

export interface ILotteCancelOrderAdvancedRequest {
  sub_no: string;
  ord_no: string;
  acnt_no: string;
  ord_frt_dt: string;
  hts_user_id: string;
  hts_user_nm: string;
  cli_ip_addr: string;
  cli_mac_addr: string;
  mdm_tp: string;
}
