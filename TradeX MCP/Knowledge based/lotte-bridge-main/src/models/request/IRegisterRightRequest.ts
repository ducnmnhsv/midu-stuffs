import { Models } from 'tradex-common';

export interface IRegisterRightRequest extends Models.IDataRequest {
  amount: number;
  bankCode: string;
  baseDate: string;
  quantity: number;
  rightType: string;
  stockCode: string;
  subNumber: string;
  bankAccount: string;
  tradeNumber: string;
  accountNumber: string;
  sequenceNumber: number;
}
