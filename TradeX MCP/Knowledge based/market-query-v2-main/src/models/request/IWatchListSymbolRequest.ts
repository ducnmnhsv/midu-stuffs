import { Models } from 'tradex-common';

export interface IWatchListSymbolRequest extends Models.IDataRequest {
  watchlistId?: number;
}
