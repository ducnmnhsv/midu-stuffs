export interface ILotteOrderBookRequest {
  acnt_no: string;
  sub_no: string;
  sellbuy_type: string;
  stock_code: string;
  mth_type: string;
  next_date: string;
  next_key: string;
  row_count: string;
}
