import { Models } from 'tradex-common';

export interface IRightHistoryRequest extends Models.IDataRequest {
  rightType: string;
  subNumber: string;
  accountNumber: string;
  stockCode: string;
  marketType: string;
  lastBaseDate: string;
  lastStockCode: string;
  lastSequenceNumber: string;
  fetchCount: number;
}
