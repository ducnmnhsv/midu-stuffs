export interface ITodayUnmatchOrderResponse {
  channel?: string;
  bankCode?: string;
  bankName?: string;
  username?: string;
  orderDate?: string;
  orderTime?: string;
  orderType?: string;
  stockCode?: string;
  subNumber?: string;
  branchCode?: string;
  marketType?: string;
  orderPrice?: number;
  orderNumber?: string;
  orderStatus?: string;
  sellBuyType?: string;
  matchedPrice?: number;
  accountNumber?: string;
  matchedAmount?: number;
  orderQuantity?: number;
  matchedQuantity?: number;
  unmatchedQuantity?: number;
  originalOrderNumber?: string;
}
