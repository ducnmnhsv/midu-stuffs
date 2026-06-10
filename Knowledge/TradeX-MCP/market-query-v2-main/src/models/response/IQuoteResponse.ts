export default interface IQuoteResponse {
  lastSize?: number;
  lastIndex?: number;
  data: IQuoteItem[];
}

export interface IQuoteItem {
  t?: string; // time (yyyyMMddhhmmss)
  o?: number; // open price
  h?: number; // high price
  l?: number; // low price
  c?: number; // close price
  ch?: number; // change
  ra?: number; // rate
  vo?: number; // trading volume
  va?: number; // trading value
  mv?: number; // matching volume
  mb?: string; // matched by
}
