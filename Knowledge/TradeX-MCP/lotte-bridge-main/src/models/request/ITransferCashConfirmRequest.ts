import { Models } from 'tradex-common';

export interface ITransferCashConfirmRequest extends Models.IDataRequest {
  accountNumber: string;
  date: string;
  subNumber: string;
  sequenceNumber: string;
}
