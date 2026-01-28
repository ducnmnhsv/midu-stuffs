import { Models } from 'tradex-common';

export interface ILoanEstimatedFeeRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  loanBankCode: string;
  settleDate: string;
  amount: string;
}
