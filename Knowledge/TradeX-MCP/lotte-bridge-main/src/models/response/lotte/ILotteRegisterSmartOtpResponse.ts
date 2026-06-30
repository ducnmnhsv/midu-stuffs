import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteRegisterSmartOtpResponse extends ILotteCommonResponse {
  data_list: ILotteRegisterSmartOtpDataList[];
}

export interface ILotteRegisterSmartOtpDataList {
  sotp_stat: string;
  sotp_sec: string;
}
