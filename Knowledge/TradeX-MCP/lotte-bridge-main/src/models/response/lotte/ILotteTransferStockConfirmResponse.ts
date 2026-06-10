import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteTransferStockConfirmResponse extends ILotteCommonResponse {
  data_list: ILotteTransferStockConfirmResponseData[];
}

export interface ILotteTransferStockConfirmResponseData {
  scrt_err_msg: string;
}
