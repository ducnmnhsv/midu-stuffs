export interface IPutthroughDealResponse {
  s: string; // symbol
  t: string; // time (yyyyMMdd)
  mp: number; // match price
  mvo: number; // match volume
  mva: number; // match value
  pvo: number; // put through volume
  pva: number; // put through value
  m: string; // marketType
}
