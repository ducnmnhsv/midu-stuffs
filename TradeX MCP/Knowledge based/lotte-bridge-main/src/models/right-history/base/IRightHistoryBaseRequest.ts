import { Models } from 'tradex-common';

export interface IRightHistoryBaseRequest extends Models.IDataRequest {
  accountNo: string;
  subNo: string;
  symbol: string;
  marketType: string;
  nextKey: string;
  fetchCount: number;
}
