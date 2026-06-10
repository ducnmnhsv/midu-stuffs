import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteCancelRightResponse extends ILotteCommonResponse {
  data_list: ILotteCancelRightResponseData[];
}

export interface ILotteCancelRightResponseData {
  scrt_err_msg: string;
}
