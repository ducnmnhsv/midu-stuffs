export interface IGetTransferCashConfirmResponse {
  status: string;
  transactionDate: string;
  sequenceNumber: string;
  amount: number;
  receivedAccountNumber: string;
  receivedSubNumber: string;
  channel: string;
  isCancel: boolean;
  note: string;
  nextKey: string;
}
