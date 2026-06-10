export interface ISymbolPrevious {
  _id?: string;
  close: number;
  previousClose?: number;
  lastTradingDate: Date;
  previousTradingDate?: Date;
  refCode?: string;
  marketType?: string;
  type?: string;
  note?: string;
  createdAt?: Date;
  updatedAt?: Date;
}
