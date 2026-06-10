export interface ISymbolQuoteMinutes {
  _id?: string;
  code?: string;
  last?: number;
  open?: number;
  high?: number;
  low?: number;
  tradingVolume?: number;
  tradingValue?: number;
  periodTradingVolume?: number;
  date?: Date;
  milliseconds?: number;
  refCode?: string;
}
