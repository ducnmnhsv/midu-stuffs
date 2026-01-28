import { Models } from 'tradex-common';

export interface ILoanAvailableRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  loanBankCode: string;
  lastSettleBankCode: number;
  lastMatchDate: string;
  lastSettleDate: string;
  lastLoanOrderType: string;
  fetchCount: number;
}
