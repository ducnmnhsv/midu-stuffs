export interface ILotteModifyOrderRequest {
  sub_no: string;
  brch_cd: string;
  ord_pri: number;
  ord_no: string;
  ord_qty: number;
  acnt_no: string;
  hts_user_id: string;
  hts_user_nm: string;
  idno: string;
  cli_ip_addr: string;
  cli_mac_addr: string;
  lang_code: string;
  mdm_tp: string;
}
