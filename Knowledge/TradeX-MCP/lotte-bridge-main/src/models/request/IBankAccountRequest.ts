import { Models } from 'tradex-common';

export interface IBankListRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
}

export interface IRegisterBankAccountRequest extends Models.IDataRequest {
  accountNumber: string;
  bankCode: string;
  bankAccountNumber: string;
  branchCode: string;
}

export interface IDeleteBankAccountRequest extends Models.IDataRequest {
  accountNumber: string;
  bankCode: string;
  bankAccountNumber: string;
  branchCode: string;
}
