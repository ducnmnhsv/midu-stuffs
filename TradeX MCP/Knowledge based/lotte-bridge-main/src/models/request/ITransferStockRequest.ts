import { Models } from 'tradex-common';

export interface ITransferStockRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  stockCode: string;
  quantity: number;
  receivedAccountNumber: string;
  receivedSubNumber: string;
  limitedQuantity: number;
  note: string;
}
