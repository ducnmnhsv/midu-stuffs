import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteLoanConfirmResponse extends ILotteCommonResponse {
  data_list: ILotteLoanConfirmResponseData[];
}

export interface ILotteLoanConfirmResponseData {
  scrt_err_msg: string;
}
