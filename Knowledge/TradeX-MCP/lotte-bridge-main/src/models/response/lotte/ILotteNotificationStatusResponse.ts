import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteNotificationStatusResponse extends ILotteCommonResponse {
  error_code: string;
  error_desc: string;
  success: boolean;
  total_record: string;
  data_list: [];
}

export interface ILotteNotificationStatusResponseData {
  status: string;
}
