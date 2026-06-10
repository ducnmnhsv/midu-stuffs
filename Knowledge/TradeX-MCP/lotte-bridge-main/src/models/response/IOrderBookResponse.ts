export interface IOrderBookResponse {
  orderTime: string;
  stockCode: string;
  sellBuyType: string;
  orderType: string;
  orderQuantity: number;
  orderPrice: number;
  matchedQuantity: number;
  matchedPrice: number;
  unmatchedQuantity: number;
  modifyCancelType: string;
  orderStatus: string;
  orderNumber: number;
  originalOrderNumber: number;
  canModifyCancel: boolean;
  orderDate: string;
  nextKey: string;
}
