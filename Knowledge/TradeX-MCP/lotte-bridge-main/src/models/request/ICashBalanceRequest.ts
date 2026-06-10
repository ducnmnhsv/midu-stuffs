import { Models } from 'tradex-common';

export interface ICashBalanceRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  bankAccount: string;
  bankCode: string;
}
