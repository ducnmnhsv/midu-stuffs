import { Models } from 'tradex-common';

export interface IRightAvailableRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  rightType: string;
  lastBaseDate: string;
  lastStockCode: string;
  lastSequenceNumber: string;
  fetchCount: number;
}
