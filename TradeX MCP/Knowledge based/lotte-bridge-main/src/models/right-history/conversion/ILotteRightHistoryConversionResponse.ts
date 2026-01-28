import { ILotteRightHistoryBaseItem, ILotteRightHistoryBaseResponse } from '../base';

export interface ILotteRightHistoryConversionItem extends ILotteRightHistoryBaseItem {
  symbol: string;
  seq: string;
  base_date: string;
  status: string;
  base_rate: string;
  cnvt_rate: string;
  own_qtty: string;
  cnvt_stk_cd: string;
  avail_qtty: string;
  effect_date: string;
  effect_yn: string;
  flotq_std_pri: string;
  flotq_amt: string;
  flotq_pay_dt: string;
  flotq_stat_yn: string;
}

// We can use the base response directly, but we redefine it here for clarity and future extensibility.
export interface ILotteRightHistoryConversionResponse
  extends ILotteRightHistoryBaseResponse<ILotteRightHistoryConversionItem> {}
