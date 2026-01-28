export interface IStockBalanceResponse {
  stockCode?: string;
  balanceQuantity?: number;
  buyAmount?: number;
  evaluationAmount?: number;
  pendingBuyQuantity?: number;
  pendingSellQuantity?: number;
  orderAvailableQuantity?: number;
  unsellableQuantity?: number;
  blockQuantity?: number;
  deliveryPendingQuantity?: number;
  totalPendingQuantity?: number;
}
