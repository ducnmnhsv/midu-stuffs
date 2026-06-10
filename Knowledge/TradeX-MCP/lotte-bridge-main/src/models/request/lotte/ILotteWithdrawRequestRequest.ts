export interface ILotteWithdrawRequestRequest {
  lang_code: string;
  acnt_no: string;
  sub_no: string;
  amount: number;
  remark: string;
  bank_code: string;
  bank_account: string;
  hts_user_id: string;
}
