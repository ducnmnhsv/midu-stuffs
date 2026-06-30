export interface IOrderStatusDerivativeRequest {
  event_code: string;
  event_seqno: string;
  date: string;
  acnt_no: string;
  evt_time: string;
  evt_ordNo: string;
  evt_orgOrdNo: string;
  evt_action: string;
  evt_account: string;
  evt_code: string;
  evt_side: string;
  evt_ordType: string;
  evt_price: string;
  evt_qty: string;
  evt_status: string;
  evt_matchQty: string;
  evt_remQty: string;
  evt_matchPrice: string;
}
