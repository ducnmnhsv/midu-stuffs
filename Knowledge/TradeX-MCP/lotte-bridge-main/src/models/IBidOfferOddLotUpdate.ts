import { IBidOfferItem } from './ISymbolInfo';

export interface IBidOfferOddLotUpdate {
  code?: string;
  last?: number;
  change?: number;
  rate?: number;
  bidOfferList?: IBidOfferItem[];
  time?: string;
}
