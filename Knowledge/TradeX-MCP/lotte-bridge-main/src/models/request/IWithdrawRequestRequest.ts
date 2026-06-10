import { Models } from 'tradex-common';

export interface IWithdrawRequestRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  amount: number;
  note: string;
  bankCode: string;
  bankAccount: string;
}
