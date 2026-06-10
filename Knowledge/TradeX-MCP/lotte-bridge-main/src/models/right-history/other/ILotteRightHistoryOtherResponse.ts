import { ILotteRightHistoryBaseItem, ILotteRightHistoryBaseResponse } from '../base';

export interface ILotteRightHistoryOtherItem extends ILotteRightHistoryBaseItem {
  symbol: string;
  seq: string;
  base_date: string;
}

// We can use the base response directly, but we redefine it here for clarity and future extensibility.
export interface ILotteRightHistoryOtherResponse extends ILotteRightHistoryBaseResponse<ILotteRightHistoryOtherItem> {}
