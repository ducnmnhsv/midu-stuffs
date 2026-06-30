export class CopyTradingOrderRequest {
  copyPortfolioId?: number;
  subScriberId?: number;
  fromDate?: Date;
  toDate?: Date;
  stockCode?: string;
  sellBuyType?: string;
  page?: number = 0;
  size?: number = 10;
}
