import { Models } from 'tradex-common';

export interface ILoanDetailRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  loanBankCode: string;
  settleBankCode: string;
  matchDate: string;
  settleDate: string;
  loanOrderType: string;
  lastSettleBankCode: string;
  lastStockCode: string;
  lastLoanOrderType: string;
  fetchCount: number;
}
