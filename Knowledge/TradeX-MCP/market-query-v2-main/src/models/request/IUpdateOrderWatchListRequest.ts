import { Models } from 'tradex-common';

export interface IUpdateOrderWatchListRequest extends Models.IDataRequest {
  watchlistId?: string;
  orderNo?: number;
}
