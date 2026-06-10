import { IRightHistoryBaseItem, IRightHistoryBaseResponse } from '../base';

export interface IRightHistoryConversionItem extends IRightHistoryBaseItem {
  symbol: string;
  sequence: number | null;
  baseDate: string;
  status: string;
  baseRate: number | null;
  conversionRate: number | null;
  ownedQuantity: number | null;
  convertedSymbol: string;
  convertedQuantity: number | null;
  effectDate: string;
  isEffective: boolean | null;
  oddLotPrice: number | null;
  oddLotAmount: number | null;
  oddLotPayDate: string;
  isOddLotPaid: boolean | null;
}

export interface IRightHistoryConversionResponse extends IRightHistoryBaseResponse<IRightHistoryConversionItem> {
  // Uses base structure: items[], nextData
}
