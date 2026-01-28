import { Models } from 'tradex-common';

export interface ICancelWithdrawRequest extends Models.IDataRequest {
  note: string;
  amount: number;
  bankCode: string;
  subNumber: string;
  bankAccount: string;
  accountNumber: string;
  sequenceNumber: string;
  transactionCode: string;
  transactionType: string;
}
