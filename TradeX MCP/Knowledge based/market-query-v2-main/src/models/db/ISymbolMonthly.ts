import { ISymbolDaily } from './ISymbolDaily';

export interface ISymbolMonthly extends ISymbolDaily {
  dayCount: number;
}
