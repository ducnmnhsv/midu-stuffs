import { Models } from 'tradex-common';

export interface ITransferStockConfirmRequest extends Models.IDataRequest {
  accountNumber: string;
  date: string;
  stockCode: string;
  sequenceNumber: string;
}
