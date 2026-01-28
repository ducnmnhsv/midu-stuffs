import { Models } from 'tradex-common';

export interface ILoanConfirmRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  loanDate: string;
  matchDate: string;
  sequenceNumber: string;
  loanAmount: string;
  username: string;
  loanBankCode: string;
}
