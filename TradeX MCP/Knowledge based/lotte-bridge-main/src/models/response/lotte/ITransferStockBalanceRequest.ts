import { Models } from 'tradex-common';

export interface ITransferStockBalanceRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  lastStockCode: string;
  fetchCount: number;
}
