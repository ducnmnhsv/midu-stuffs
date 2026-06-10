import { Models } from 'tradex-common';

export interface ILoanHistoryRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  loanBankCode: string;
  lastLoanDate: string;
  lastLoanBankCode: string;
  lastMatchDate: string;
  lastStockCode: string;
  fetchCount: number;
}
