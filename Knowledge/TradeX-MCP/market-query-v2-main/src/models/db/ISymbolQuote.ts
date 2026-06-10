export interface ISymbolQuote {
  _id?: string;
  code?: string;
  open?: number;
  time?: string;
  date?: Date;
  high?: number;
  low?: number;
  last?: number;
  change?: number;
  rate?: number;
  tradingVolume?: number;
  tradingValue?: number;
  matchingVolume?: number;
  sequence?: number;
  matchedBy?: string;
  ceilingFloorEqual?: string;
	totalOfferVolume?: number;
	totalBidVolume?: number;
}
