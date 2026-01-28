export interface ILotteTransferCashRequest {
  hts_user_id: string;
  lang_code: string;
  acnt_no: string;
  sub_no: string;
  amount: number;
  recv_sub: string;
  remark: string;
}
