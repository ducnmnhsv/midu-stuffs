import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteAccountInfoResponse extends ILotteCommonResponse {
  data_list: ILotteAccountInfoData[];
}

export interface ILotteAccountInfoData {
  customer_name: string;
  identity_card: string;
  phone: string;
  email: string;
  address: string;
  manager: string;
  emp_no: string;
  cust_eng_nm: string;
  grp_tp: string;
  frgn_tp: string;
  sex_tp: string;
  idno_iss_dt: string;
  idno_iss_orga: string;
  birth_dt: string;
  ctry_cd: string;
  office_nm: string;
  job_cd: string;
  conct_tel_tp: string;
  conct_addr_tp: string;
  fax: string;
  home_tel: string;
  office_tel: string;
  office_addr: string;
  tax_cd: string;
  idno_tp: string;
  staff_yn: string;
  idno_expr_dt: string;
  cont_addr: string;
  mobile_2: string;
}
