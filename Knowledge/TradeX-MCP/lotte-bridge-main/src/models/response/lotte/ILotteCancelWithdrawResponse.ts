import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotterCancelWithdrawResponse extends ILotteCommonResponse {
  data_list: ILotterCancelWithdrawResponseData[];
}

export interface ILotterCancelWithdrawResponseData {}
