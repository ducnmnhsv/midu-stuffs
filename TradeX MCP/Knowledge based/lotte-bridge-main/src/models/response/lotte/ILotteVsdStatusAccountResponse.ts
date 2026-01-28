import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteVsdStatusAccountResponse extends ILotteCommonResponse {
  data_list: ILotteVsdStatusAccountResponseData[];
}

export interface ILotteVsdStatusAccountResponseData {
  val: string;
}
