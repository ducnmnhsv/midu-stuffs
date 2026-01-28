import { BaseRequest } from 'tradex-models-common';

export interface IQuerySymbolHistoryEventsRequest extends BaseRequest {
  symbol: string;
  from: string;
  to: string;
  resolution: string;
}
