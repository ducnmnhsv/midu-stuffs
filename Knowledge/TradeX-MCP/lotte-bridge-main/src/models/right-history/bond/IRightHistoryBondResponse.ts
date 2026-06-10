import { IRightHistoryBaseItem, IRightHistoryBaseResponse } from '../base';

export interface IRightHistoryBondItem extends IRightHistoryBaseItem {}

export interface IRightHistoryBondResponse extends IRightHistoryBaseResponse<IRightHistoryBondItem> {
  // Uses base structure: items[], nextData
}
