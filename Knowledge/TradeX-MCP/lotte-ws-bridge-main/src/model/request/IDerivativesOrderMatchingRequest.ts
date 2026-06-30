export interface IDerivativesOrderMatchingRequest {
  date: string;
  event_seqno: string;
  event_code: string;
  acnt_no: string;
  series: string;
  sb_tp: string;
  mth_qty: string;
  mth_pri: string;
  mth_time?: string;
}
