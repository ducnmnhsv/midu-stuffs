import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteVerifySmartOtpResponse extends ILotteCommonResponse {
  data_list: ILotteVerifySmartOtpDataList[];
}

export interface ILotteVerifySmartOtpDataList {
  auth_result: string; // 'AUTHENTICATED' | 'NOT_AUTHENTICATED'
}
