import { ILotteRightHistoryBaseItem, ILotteRightHistoryBaseResponse } from '../base';

export interface ILotteRightHistoryBondItem extends ILotteRightHistoryBaseItem {
  symbol: string;
  seq: string;
  base_date: string;
  status: string;
  base_rate: string;
  divd_rate: string;
  own_qtty: string;
  begin_date: string;
  end_date: string;
  issue_price: string;
  avail_qtty: string;
  req_qtty: string;
  req_amt: string;
  effect_date: string;
  effect_yn: string;
}

// We can use the base response directly, but we redefine it here for clarity and future extensibility.
export interface ILotteRightHistoryBondResponse extends ILotteRightHistoryBaseResponse<ILotteRightHistoryBondItem> {}
