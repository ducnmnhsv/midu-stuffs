export interface ISellableResponse {
  stockCode?: string;
  balanceQuantity?: number;
  t2Sell?: number;
  t2Buy?: number;
  t1Sell?: number;
  t1Buy?: number;
  todaySell?: number;
  todayBuy?: number;
  sellableQuantity?: number;
}
