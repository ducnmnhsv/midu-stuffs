export interface ITransferCashHistoryResponse {
  note?: string;
  amount?: number;
  isCancel?: boolean;
  subNumber?: string;
  accountNumber?: string;
  sequenceNumber?: number;
  transactionDate?: string;
  receivedSubNumber?: string;
  sendSequenceNumber?: number;
  receivedAccountName?: string;
  receiveSequenceNumber?: number;
  receivedAccountNumber?: string;
  transferSequenceNumber?: number;
  next?: string;
}
