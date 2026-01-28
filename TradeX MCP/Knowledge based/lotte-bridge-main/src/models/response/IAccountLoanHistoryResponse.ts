export interface IAccountLoanHistoryResponse {
  loanDate?: string;
  expiredDate?: string;
  stockCode?: string;
  loanType?: string;
  loanQuantity?: number;
  loanAmount?: number;
  loanInterest?: number;
  loanRepayAmount?: number;
  loanRemainAmount?: number;
  status?: string;
  totalLoan?: number;
  nextKey?: string;
}
