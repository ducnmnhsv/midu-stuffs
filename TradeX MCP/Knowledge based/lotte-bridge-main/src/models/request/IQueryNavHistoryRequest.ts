import { IDataRequest } from 'tradex-common/build/src/modules/models';

export interface IQueryNavHistoryRequest extends IDataRequest {
  accountNumber: string;
  subNumber: string;
  fromDate: string;
  toDate: string;
  nextKey: string;
  fetchCount: number;
}
