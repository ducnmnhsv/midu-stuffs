import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteEmployeeInfoResponse extends ILotteCommonResponse {
  data_list: ILotteEmployeeInfoData[];
}

export interface ILotteEmployeeInfoData {
  os_id: string;
  os_user_nm: string;
  os_dept_cd: string;
  os_dept_name: string;
  os_addr: string;
  os_email: string;
  os_home_phone: string;
  os_brith: string;
  os_real_number: string;
  os_sex: string;
  os_post_code: string;
  os_post_name: string;
  os_brch: string;
  os_emp_tp: string;
  os_rcm_emp_no: string;
  os_emp_type: string;
  os_mng_emp_no: string;
  os_mng_tp: string;
}



