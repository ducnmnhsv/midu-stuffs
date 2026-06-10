import { Models } from 'tradex-common';

export interface ITransferStockHistoryRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  fromDate: string;
  toDate: string;
  branchCode: string;
  lastTransactionDate: string;
  lastReceivedAccountNumber: string;
  lastReceivedSubNumber: string;
  lastSequenceNumber: number;
  fetchCount: number;
  next: string;
}
