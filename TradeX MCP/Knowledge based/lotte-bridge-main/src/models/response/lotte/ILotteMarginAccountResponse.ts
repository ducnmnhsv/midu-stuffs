import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteMarginAccountResponse extends ILotteCommonResponse {
  data_list: ILotteMarginAccountResponseData[];
}

export interface ILotteMarginAccountResponseData {
  ssr: string;
  loan_pri_max: string;
}
