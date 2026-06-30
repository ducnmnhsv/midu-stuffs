import { Models } from 'tradex-common';

export interface IChangeBrokerHistoryRequest extends Models.IDataRequest {
  accountNumber: string;
  status?: string;
  fromDate?: string;
  toDate?: string;
  fetchCount?: number;
  nextKey?: string;
}
