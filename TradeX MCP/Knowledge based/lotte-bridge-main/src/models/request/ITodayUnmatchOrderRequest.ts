import { Models } from 'tradex-common';

export interface ITodayUnmatchOrderRequest extends Models.IDataRequest {
  stockCode: string;
  lastBranchCode: string;
  lastOrderNumber: string;
  lastOrderPrice: string;
  fetchCount: number;
  accountNumber: string;
  date: string;
  subNumber: string;
}
