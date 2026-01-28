import { ISymbolDaily } from './ISymbolDaily';

export interface ISymbolWeeklyOrMonthly extends ISymbolDaily {
  dayCount: number;
}
