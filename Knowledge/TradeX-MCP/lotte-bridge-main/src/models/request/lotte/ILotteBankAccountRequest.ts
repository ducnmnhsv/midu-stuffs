export interface ILotteRegisterBankAccountRequest {
  acnt_no: string;
  bank_cd: string;
  bank_acnt_no: string;
  bank_acnt_nm: string;
  branch: string;
  hts_user_id: string;
}

export interface ILotteDeleteBankAccountRequest {
  acnt_no: string;
  bank_cd: string;
  bank_acnt_no: string;
  bank_acnt_nm: string;
  branch: string;
  hts_user_id: string;
}
