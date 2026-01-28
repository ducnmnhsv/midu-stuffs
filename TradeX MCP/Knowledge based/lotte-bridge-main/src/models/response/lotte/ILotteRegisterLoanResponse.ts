import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteRegisterLoanResponse extends ILotteCommonResponse {
  data_list: ILotteRegisterLoanResponseData[];
}

export interface ILotteRegisterLoanResponseData {}
