import { Models } from 'tradex-common';

export interface ITransferCashRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  receivedSubNumber: string;
  amount: number;
  note: string;
  receivedAccountNumber: string;
}
