export interface ILoanAvailableResponse {
  tax?: number;
  feeRate?: number;
  matchDate?: string;
  loanPeriod?: number;
  settleDate?: string;
  tradingFee?: number;
  matchAmount?: number;
  adjustAmount?: number;
  estimatedFee?: number;
  loanBankName?: string;
  loanOrderName?: string;
  loanOrderType?: string;
  possibleAmount?: number;
  settleBankCode?: string;
}
