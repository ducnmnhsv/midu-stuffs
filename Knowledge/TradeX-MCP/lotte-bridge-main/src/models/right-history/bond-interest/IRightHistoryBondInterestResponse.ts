import { IRightHistoryBaseItem, IRightHistoryBaseResponse } from '../base';

export interface IRightHistoryBondInterestItem extends IRightHistoryBaseItem {
  symbol: string;
  sequence: number | null;
  baseDate: string;
  status: string;
  distributionRate: number | null;
  taxRate: number | null;
  ownedQuantity: number | null;
  basePrice: number | null;
  principalAmount: number | null;
  principalPayDate: string;
  isPrincipalReceived: boolean | null;
  interestAmount: number | null;
  interestPayDate: string;
  isInterestPaid: boolean | null;
}

export interface IRightHistoryBondInterestResponse extends IRightHistoryBaseResponse<IRightHistoryBondInterestItem> {
  // Uses base structure: items[], nextData
}
