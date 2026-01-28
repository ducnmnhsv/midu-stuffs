export interface ILotteTransferStockRequest {
  hts_user_id: string;
  acnt_no: string;
  sub_no: string;
  acnt_r: string;
  sub_r: string;
  stk_cd: string;
  qty: number;
  lmt_qty: number;
  cnte: string;
}
