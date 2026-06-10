import { ISymbolDaily } from '../db/ISymbolDaily';

export interface IGroupedSymbolDailyResponse {
  _id: string;
  items: ISymbolDaily[];
}
