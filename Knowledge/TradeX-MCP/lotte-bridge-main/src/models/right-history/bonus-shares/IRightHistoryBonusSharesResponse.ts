import { IRightHistoryBaseItem, IRightHistoryBaseResponse } from '../base';

export interface IRightHistoryBonusSharesItem extends IRightHistoryBaseItem {
  symbol: string;
  sequence: number;
  baseDate: string;
  status: string;
  baseRate: number;
  dividendRate: number;
  ownedQuantity: number;
  availableQuantity: number;
  effectDate: string;
  isEffective: boolean | null;
  oddLotPrice: number;
  oddLotAmount: number;
  oddLotPayDate: string;
  isOddLotPaid: boolean | null;
}

export interface IRightHistoryBonusSharesResponse extends IRightHistoryBaseResponse<IRightHistoryBonusSharesItem> {
  // Uses base structure: items[], nextData
}
