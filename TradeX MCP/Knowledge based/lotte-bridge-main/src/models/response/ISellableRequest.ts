import { Models } from 'tradex-common';

export interface ISellableRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  date: string;
  stockCode: string;
  fetchCount: number;
}
