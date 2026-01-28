export interface ILoanHistoryResponse {
  status?: string;
  loanDate?: string;
  matchDate?: string;
  stockCode?: string;
  loanAmount?: number;
  matchAmount?: number;
  loanBankCode?: string;
  loanBankName?: string;
  matchQuantity?: number;
  loanRepayAmount?: number;
  loanRemainAmount?: number;
}
