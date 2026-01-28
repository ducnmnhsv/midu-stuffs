import { ILotteRightHistoryBaseItem, ILotteRightHistoryBaseResponse } from '../base';

export interface ILotteRightHistoryBondInterestItem extends ILotteRightHistoryBaseItem {
  symbol: string;
  seq: string;
  base_date: string;
  status: string;
  divd_rate: string;
  tax_rate: string;
  own_qtty: string;
  issue_price: string;
  asn_amt: string;
  list_dt: string;
  rcpt_trd_no_yn: string;
  inter_amt: string;
  inter_dt: string;
  effect_yn: string;
}

// We can use the base response directly, but we redefine it here for clarity and future extensibility.
export interface ILotteRightHistoryBondInterestResponse
  extends ILotteRightHistoryBaseResponse<ILotteRightHistoryBondInterestItem> {}
