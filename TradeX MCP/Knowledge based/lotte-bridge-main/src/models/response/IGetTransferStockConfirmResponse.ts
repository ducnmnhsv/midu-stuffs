export interface IGetTransferStockConfirmResponse {
  status: string;
  transactionDate: string;
  sequenceNumber: string;
  receivedAccountNumber: string;
  receivedSubNumber: string;
  stockCode: string;
  quantity: number;
  limitedQuantity: number;
  note: string;
  nextKey: string;
}
