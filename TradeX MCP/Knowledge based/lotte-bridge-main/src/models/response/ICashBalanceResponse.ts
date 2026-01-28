export interface ICashBalanceResponse {
  depositAmount?: number;
  depositBlockAmount?: number;
  expiredLoanAmount?: number;
  waitSellAmount?: number;
  stockEvaluationAmount?: number;
  marginLoanAmount?: number;
  securedLoanAmount?: number;
  orderBlockAmount?: number;
  withdrawableAmount?: number;
  reuseAmount?: number;
  virtualDeposit?: number;
  usedVirtualDeposit?: number;
}
