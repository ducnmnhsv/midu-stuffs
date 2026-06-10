import { IRightHistoryBaseItem, IRightHistoryBaseResponse } from '../base';

export interface IRightHistoryIssueItem extends IRightHistoryBaseItem {
  symbol: string;
  sequence: number | null;
  baseDate: string;
  status: string;
  baseRate: number | null;
  dividendRate: number | null;
  ownedQuantity: number | null;
  beginDate: string;
  endDate: string;
  issuePrice: number | null;
  availableQuantity: number | null;
  requestedQuantity: number | null;
  requestedAmount: number | null;
  effectDate: string | null;
  isEffective: boolean | null;
}

export interface IRightHistoryIssueResponse extends IRightHistoryBaseResponse<IRightHistoryIssueItem> {
  // Uses base structure: items[], nextData
}
