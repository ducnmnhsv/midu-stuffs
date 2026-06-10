import { IRightHistoryBaseItem, IRightHistoryBaseResponse } from '../base';

export interface IRightHistoryOtherItem extends IRightHistoryBaseItem {
  symbol: string;
  sequence: number | string;
  baseDate: string;
}

export interface IRightHistoryOtherResponse extends IRightHistoryBaseResponse<IRightHistoryOtherItem> {
  // Uses base structure: items[], nextData
}
