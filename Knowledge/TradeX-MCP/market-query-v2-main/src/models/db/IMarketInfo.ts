export interface IMarketInfo {
  _id: string;
  lastTradingDate: Date;
  eventList: IDividendRate[];
  createdAt: Date;
  updatedAt: Date;
}

export interface IDividendRate {
  code: string;
  basicPrice: number;
  totalAdjustRate: number;
}
