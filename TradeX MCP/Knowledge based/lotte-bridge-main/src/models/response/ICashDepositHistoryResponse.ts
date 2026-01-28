export interface ICashDepositHistoryItemResponse {
  tradeDate?: string;
  sequence?: number;
  remarkName?: string;
  cashIn?: number;
  cashOut?: number;
  cumulativeBalance?: number;
  note?: string;
  remarkCode?: string;
  balanceBegin?: number;
  balanceEnd?: number;
  dateBegin?: string;
  unsettledBuyAmount?: number;
  dateEnd?: string;
  description?: string;
  nextKey?: string;
}