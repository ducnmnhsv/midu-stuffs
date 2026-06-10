import { Models } from 'tradex-common';

export interface IAccountProfitLossRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  bankCode: string;
  lastStockCode: string;
  fetchCount: number;
  bankName: string;
}
