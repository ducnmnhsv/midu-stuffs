import { Models } from 'tradex-common';

export enum TransferCashHistoryStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  CANCELLED = 'CANCELLED',
  APPROVED_INTERNAL = 'APPROVED_INTERNAL',
}

export interface ITransferCashHistoryRequest extends Models.IDataRequest {
  status: TransferCashHistoryStatus;
  accountNumber: string;
  subNumber: string;
  fromDate: string;
  toDate: string;
  fetchCount: number;
  next: string;
}
