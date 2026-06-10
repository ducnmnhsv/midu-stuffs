import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteTransferStockBalanceResponse extends ILotteCommonResponse {
  data_list: ILotteTransferStockBalanceResponseData[];
}

export interface ILotteTransferStockBalanceResponseData {
  stk_cd: string;
  stk_mn: string;
  able_qty: string;
  able_limt_qty: string;
}
