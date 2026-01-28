export interface IGetLoanConfirmResponse {
  status: string;
  loanDate: string;
  matchDate: string;
  sequenceNumber: string;
  stockCode: string;
  loanPeriod: number;
  feeRate: number;
  amount: number;
  channel: string;
  loanBankCode: string;
  nextKey: string;
}
