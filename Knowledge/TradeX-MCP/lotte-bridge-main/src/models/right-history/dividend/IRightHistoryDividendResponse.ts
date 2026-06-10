import { IRightHistoryBaseItem, IRightHistoryBaseResponse } from '../base';

export interface IRightHistoryDividendItem extends IRightHistoryBaseItem {
  symbol: string;
  sequence: number;
  baseDate: string;
  status: string;
  baseRate: number;
  dividendRate: number;
  ownedQty: number;
  availableDividendQty: number;
  effectDate: string;
  isEffective: boolean | null;
  taxRate: number;
  cashDivAmount: number;
  dividendPayDate: string;
  isCashDivReceive: boolean | null;
  oddLotPrice: number;
  oddLotAmount: number;
  oddLotPayDate: string;
  isOddLotPaid: boolean | null;
}

export interface IRightHistoryDividendResponse extends IRightHistoryBaseResponse<IRightHistoryDividendItem> {
  // Uses base structure: items[], nextData
}
