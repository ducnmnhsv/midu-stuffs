export interface ISymbolStatisticsRequest {
  symbol: string; // symbol code
  pageSize: number;
  pageNumber: number;
  sortBy?: string;
}
