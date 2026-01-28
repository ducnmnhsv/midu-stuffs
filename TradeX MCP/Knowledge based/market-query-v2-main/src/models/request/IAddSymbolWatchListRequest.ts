import { Models } from 'tradex-common';

export interface IAddSymbolWatchListRequest extends Models.IDataRequest {
  watchlistId?: number[];
  symbol?: string[];
}
