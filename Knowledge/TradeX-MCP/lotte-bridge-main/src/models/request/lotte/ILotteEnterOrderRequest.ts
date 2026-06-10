import { OrderType } from '../../../constants/enum';

export interface ILotteNornalOrderRequest {
  stk_cd: string;
  acnt_no: string;
  sub_no: string;
  ord_pri: number;
  ord_qty: number;
  bank_cd: string;
  stk_ord_tp: OrderType;
  hts_user_id: string;
  hts_user_nm: string;
  cli_ip_addr: string;
  cli_mac_addr: string;
  idno: string;
  lang_code: string;
  mdm_tp: string;
}

export interface ILotteAdvancedOrderRequest {
  bank_code: string;
  stk_ord_tp: string;
  stk_cd: string;
  sub_no: string;
  ord_pri: number;
  bank_acnt: string;
  phone_num: string;
  acnt_no: string;
  ord_qty: number;
  hts_user_id: string;
  hts_user_nm: string;
  cli_ip_addr: string;
  cli_mac_addr: string;
  idno: string;
  mdm_tp: string;
}
