export interface ILotteGetTransferStockConfirmRequest {
  status: string;
  from_dt: string;
  to_dt: string;
  acnt_no: string;
  sub_no: string;
  next: string;
  hts_user_id: string;
  stk_cd: string;
  row_count: string;
}
