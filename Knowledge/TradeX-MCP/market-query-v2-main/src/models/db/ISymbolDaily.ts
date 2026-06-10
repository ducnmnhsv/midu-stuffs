export interface ISymbolDaily {
  _id?: string;
  code: string;
  open: number;
  high: number;
  low: number;
  last: number;
  change?: number;
  rate?: number;
  tradingVolume: number;
  tradingValue: number;
  date: Date;
  refCode?: string;
  returns?: number;
}
