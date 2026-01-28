import { IDataRequest } from 'tradex-common/build/src/modules/models';
import { MarketType, SellBuyTypeLotte, SortType } from '../../constants/enum';

export interface IHistoryOrderRequest extends IDataRequest {
  accountNumber: string;
  subNumber: string;
  fromDate: string;
  toDate: string;
  sellBuyType: SellBuyTypeLotte;
  stockCode: string;
  sortType: SortType;
  matchType: string;
  lastOrderDate: string;
  marketType: MarketType;
  lastOrderNumber: string;
  lastMatchPrice: string;
  lastBranchCode: string;
  fetchCount: number;
  nextKey: string;
}

export interface IHistoryOrderAdvancedRequest extends IDataRequest {
  stockCode: string;
  marketType: MarketType;
  sellBuyType: SellBuyTypeLotte;
  lastOrderDate: string;
  lastOrderNumber: string;
  fetchCount: number;
  accountNumber: string;
  subNumber: string;
}
