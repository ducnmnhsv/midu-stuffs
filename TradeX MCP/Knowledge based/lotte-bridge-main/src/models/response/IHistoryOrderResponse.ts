export interface IHistoryOrderResponse {
  bankName?: string;
  username?: string;
  orderDate?: string;
  orderTime?: string;
  orderType?: string;
  stockCode?: string;
  subNumber?: string;
  orderPrice?: number;
  orderAmount?: number;
  orderNumber?: string;
  orderStatus?: string;
  sellBuyType?: string;
  matchedPrice?: number;
  rejectReason?: string;
  accountNumber?: string;
  matchedAmount?: number;
  orderQuantity?: number;
  matchedQuantity?: number;
  modifyCancelType?: string;
  unmatchedQuantity?: number;
  originalOrderNumber?: string;
  modifyCancelQuantity?: number;
  nextKey?: string;
}

export interface IHistoryOrderAdvancedResponse {
  orderDate?: string;
  orderTime?: string;
  orderNumber?: string;
  stockCode?: string;
  sellBuyType?: string;
  orderType?: string;
  orderQuantity?: number;
  orderPrice?: number;
  username?: string;
  channel?: string;
  orderStatus?: string;
}
