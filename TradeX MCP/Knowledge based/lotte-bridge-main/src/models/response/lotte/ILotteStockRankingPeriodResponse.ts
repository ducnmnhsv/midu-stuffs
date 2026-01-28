import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteStockRankingPeriodResponse extends ILotteCommonResponse {
  data_list: ILotteStockRankingPeriodResponseData[];
}

export interface ILotteStockRankingPeriodResponseData {
  hasNext: string;
  nextKey: string;
  list: ILotteStockRankingPeriodResponseDataListItem[];
}

export interface ILotteStockRankingPeriodResponseDataListItem {
  seq: string;
  code: string;
  codeExt: string;
  status: string;
  last: string;
  change: string;
  changeRate: string;
  volume: string;
  upDownRate: string;
  upDownRange: string;
  startPrice: string;
  endPrice: string;
}
