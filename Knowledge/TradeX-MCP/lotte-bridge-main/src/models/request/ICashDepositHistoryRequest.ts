import { Models } from 'tradex-common';

export interface ICashDepositHistoryRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  nextKey?: string;
  fromDate?: string;
  toDate?: string;
  type?: string;
  fetchCount?: number;
}
