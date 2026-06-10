import { Models } from 'tradex-common';

export interface IWatchListRequest extends Models.IDataRequest {
  watchlistId?: string;
  watchlistName?: string;
}
