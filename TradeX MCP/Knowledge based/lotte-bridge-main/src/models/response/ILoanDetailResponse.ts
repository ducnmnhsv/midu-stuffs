export interface ILoanDetailResponse {
  tax?: number;
  matchDate?: string;
  stockCode?: string;
  settleDate?: string;
  tradingFee?: number;
  matchAmount?: number;
  adjustAmount?: number;
  loanOrderType?: string;
  matchQuantity?: number;
  possibleAmount?: number;
  settleBankCode?: string;
  settleBankName?: string;
}
