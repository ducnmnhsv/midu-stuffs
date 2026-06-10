export interface ILotteCashDepositHistoryRequest {
  acnt_no: string;
  sub_no: string;
  next_key?: string;
  begin_dt?: string;
  end_dt?: string;
  type?: string;
  lang_code?: string;
  row_count?: string;
}
