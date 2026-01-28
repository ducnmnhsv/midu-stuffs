import { Models } from 'tradex-common';

export interface IAccountLoanHistoryRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  nextKey: string;
  fetchCount: number;
}
