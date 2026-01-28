import { Models } from 'tradex-common';

export interface IUpcomingRightRequest extends Models.IDataRequest {
  accountNo: string;
  subNo: string;
  nextKey: string;
  fetchCount: number;
}
