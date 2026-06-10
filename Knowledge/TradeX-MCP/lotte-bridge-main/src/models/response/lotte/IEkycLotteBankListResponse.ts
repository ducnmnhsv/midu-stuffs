import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface IEkycLotteBankListResponse extends ILotteCommonResponse {
  data_list: IEkycLotteBankListResponseData[];
}

export interface IEkycLotteBankListResponseData {
  code: string;
  code_nm: string;
}
