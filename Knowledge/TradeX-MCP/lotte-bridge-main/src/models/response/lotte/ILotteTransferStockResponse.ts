import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteTransferStockResponse extends ILotteCommonResponse {
  data_list: ILotteTransferStockResponseData[];
}

export interface ILotteTransferStockResponseData {
  scrt_err_msg: string;
}
