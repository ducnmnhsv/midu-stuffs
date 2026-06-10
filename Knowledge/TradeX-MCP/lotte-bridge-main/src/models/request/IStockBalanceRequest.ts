import { Models } from 'tradex-common';

export interface IStockBalanceRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  lastStockCode: string;
  fetchCount: number;
  bankName: string;
}
