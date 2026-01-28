export interface IAccountProfitLossResponse {
  t1Deposit?: number;
  t2Deposit?: number;
  tTradeValue?: number;
  depositAmount?: number;
  totalBuyAmount?: number;
  profitLossItems?: IProfitLossItem[];
  totalProfitLoss?: number;
  estimatedDeposit?: number;
  totalProfitLossRate?: number;
  totalEvaluationAmount?: number;
}

export interface IProfitLossItem {
  t1Buy?: number;
  t2Buy?: number;
  t1Sell?: number;
  t2Sell?: number;
  todayBuy?: number;
  stockCode?: string;
  todaySell?: number;
  profitLoss?: number;
  buyingPrice?: number;
  buyingAmount?: number;
  currentPrice?: number;
  buyingQuantity?: number;
  profitLossRate?: number;
  balanceQuantity?: number;
  evaluationAmount?: number;
  sellableQuantity?: number;
  securedQuantity?: number;
}
