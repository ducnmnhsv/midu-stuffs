import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteTransferCashConfirmResponse extends ILotteCommonResponse {
  data_list: ILotteTransferCashConfirmResponseData[];
}

export interface ILotteTransferCashConfirmResponseData {
  scrt_err_msg: string;
}
