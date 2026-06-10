import { Models } from 'tradex-common';

export interface IRemoveSymbolWatchListRequest extends Models.IDataRequest {
  watchlistId?: number[];
  symbol?: string;
}
