import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteResetPasswordResponse extends ILotteCommonResponse {
  data_list: ILotteResetPasswordResponseData[];
}

export interface ILotteResetPasswordResponseData {}
