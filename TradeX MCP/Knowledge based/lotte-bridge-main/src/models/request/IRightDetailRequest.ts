import { Models } from 'tradex-common';

export interface IRightDetailRequest extends Models.IDataRequest {
  stockCode: string;
  accountNumber: string;
  subNumber: string;
  baseDate: string;
  rightType: string;
  sequenceNumber: number;
  bankCode: string;
  bankAccount: string;
  bankName: string;
}
