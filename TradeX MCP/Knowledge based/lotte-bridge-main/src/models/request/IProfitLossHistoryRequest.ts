import { Models } from 'tradex-common';

export interface IProfitLossHistoryRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  fromDate: string;
  toDate: string;
  nextKey: string;
  fetchCount: number;
  stockCode: string;
}
