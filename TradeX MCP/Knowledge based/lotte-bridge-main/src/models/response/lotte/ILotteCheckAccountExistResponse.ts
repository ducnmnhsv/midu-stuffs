import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteCheckAccountExistResponse extends ILotteCommonResponse {
  code: string;
  error_desc: string;
  success: boolean;
  total_record: string;
  data_list: [];
}
