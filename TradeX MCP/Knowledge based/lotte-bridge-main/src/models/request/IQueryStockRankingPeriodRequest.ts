export interface IQueryStockRankingPeriodRequest {
  marketType?: string;
  ranking: string;
  period: number;
  pageNumber?: number;
  pageSize?: number;
}
