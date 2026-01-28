export interface IOrderConfirmResponse {}

export interface IOrderConfirmHistoryResponse {
  accountName?: string;
  branchCode?: string;
  accountNumber?: string;
  address?: string;
  phoneNumber?: string;
  orderDate?: string;
  orderTime?: string;
  cancelType?: string;
  sellBuyType?: string;
  orderNumber?: string;
  stockCode?: string;
  orderQuantity?: number;
  orderPrice?: number;
  matchedQuantity?: number;
  matchedPrice?: number;
  subNumber?: string;
  orderValue?: number;
  market?: string;
  confirmStatus?: string;
  broker?: string;
  mediaType?: string;
  matchedValue?: number;
  orderType?: string;
  nextKey?: string;
}
