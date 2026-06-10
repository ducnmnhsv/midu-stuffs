import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteIndexListResponse extends ILotteCommonResponse {
  data_list: ILotteIndexListDataList[];
}

export interface ILotteIndexListDataList {
  list: ILotteIndexListData[];
  hasNext: string;
  nextKey: string;
}

export interface ILotteIndexListData {
  symbol: string;
  code: string;
  exchange: string;
  englishName: string;
  vietnameseName: string;
  type: string;
}

export interface ILotteIndexStockListResponse extends ILotteCommonResponse {
  data_list: ILotteIndexStockListDataList[];
}

export interface ILotteIndexStockListDataList {
  list: ILotteIndexStockListData[];
  hasNext: string;
  nextKey: string;
}

export interface ILotteIndexStockListData {
  code: string;
  vietnameseName: string;
  englishName: string;
  marketType: string;
  marketName: string;
  last: string;
  change: string;
  changeRate: string;
  refPrice: string;
  avgPrice: string;
  ceiling: string;
  floor: string;
  open: string;
  high: string;
  highTime: string;
  low: string;
  lowTime: string;
  projectOpen: string;
  controlCode: string;
}
