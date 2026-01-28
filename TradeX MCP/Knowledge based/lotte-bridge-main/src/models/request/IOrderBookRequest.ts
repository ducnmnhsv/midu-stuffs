import { Models } from 'tradex-common';
import { MatchType, SellBuyTypeLotte } from '../../constants/enum';

export interface IOrderBookRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  sellBuyType: SellBuyTypeLotte;
  stockCode: string;
  matchType: MatchType;
  lastOrderDate: string;
  nextKey: string;
  fetchCount: number;
}
