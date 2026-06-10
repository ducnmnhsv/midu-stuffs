export interface IProfitLossHistoryResponse {
  totalSellAmount?: number;
  totalCostValue?: number;
  totalProfitLoss?: number;
  totalProfitLossRatio?: number;
  items?: IProfitLossHistoryResponseItem[];
}

export interface IProfitLossHistoryResponseItem {
  accountNumber?: string;
  subNumber?: string;
  tradingDate?: string;
  nextKey?: string;
  stockCode?: string;
  saleVolume?: number;
  capitalCost?: number;
  sellPrice?: number;
  feeAndTax?: number;
  sellAmount?: number;
  costValue?: number;
  profitLoss?: number;
  profitLossRatio?: number;
}
