import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface IEkycLottePartnerNameResponse extends ILotteCommonResponse {
  data_list: IEkycLottePartnerNameResponseData[];
}

export interface IEkycLottePartnerNameResponseData {
  code_nm: string;
}
