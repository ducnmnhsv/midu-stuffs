export interface IWithdrawHistoryResponse {
  bank?: string;
  note?: string;
  amount?: number;
  approver?: string;
  bankCode?: string;
  bankName?: string;
  isCancel?: boolean;
  bankAccount?: string;
  approvalDate?: string;
  sequenceNumber?: number;
  transactionCode?: string;
  transactionDate?: string;
  transactionType?: string;
  transactionSequenceNumber?: number;
  next?: string;
}
