import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteVerifyUserResponse extends ILotteCommonResponse {
  data_list: ILotteVerifyUserDataList[];
}

export interface ILotteVerifyUserDataList {
  login_id: string;
  id_no: string;
  user_name: string;
  sec_pwd: string;
  is_exist: boolean;
  hts_level: string;
  dept_code1: string;
  dept_code2: string;
  agc_no: string;
  dept_code0: string;
  err_cnt: string;
  otp_stat: string;
  otp_index: string;
  otp_pass: string;
  sotp_stat: string;
  sotp_sec: string;
  accounts: ILotteVerifyUserAccounts[];
}

export interface ILotteVerifyUserAccounts {
  acnt_no: string;
  acnt_scrt: string;
  acnt_tp: string;
  acnt_nm: string;
  next_key: string;
  bankInfo: ILotteVerifyUserBankInfo[];
}

export interface ILotteVerifyUserBankInfo {
  bank_code: string;
  bank_name: string;
}
