export interface IQuoteRequest {
  symbol: string; // symbol code
  lastIndex?: number;
  lastSize?: number;
  fetchCount?: number;
}
