import { Models } from 'tradex-common';

export interface IWatchListIncludeSymbolRequest extends Models.IDataRequest {
  symbol?: string;
}
