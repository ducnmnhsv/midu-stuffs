import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteRegisterRightResponse extends ILotteCommonResponse {
  data_list: ILotteRegisterRightResponseData[];
}

export interface ILotteRegisterRightResponseData {
  scrt_err_msg: string;
}
