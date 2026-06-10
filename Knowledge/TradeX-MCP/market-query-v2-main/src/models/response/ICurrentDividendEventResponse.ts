import { IDividendRate } from '../db/IMarketInfo';

export interface ICurrentDividendEventResponse {
  eventList?: IDividendRate[];
  date?: string;
}
