export interface IStockRankingPeriodResponse {
  sq: number;
  s: string;
  c: number;
  ch: number;
  ra: number;
  vo: number;
  udra: number;
  udrg: number;
  sp: number;
  ep: number;
}

export interface IRedisStockRankingPeriodItemResponse {
  sequence: number;
  stockCode: string;
  last: number;
  change: number;
  rate: number;
  volume: number;
  upDownRate: number;
  upDownRange: number;
  startPrice: number;
  endPrice: number;
}

export interface IRedisStockRankingPeriodResponse {
  symbols: IRedisStockRankingPeriodItemResponse[];
  resultAt: string;
}
