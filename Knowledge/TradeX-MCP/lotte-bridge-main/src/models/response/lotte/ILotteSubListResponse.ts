import { SubType } from '../../../constants/enum';
import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteSubListResponse extends ILotteCommonResponse {
  data_list: ILotteSubListResponseData[];
}

export interface ILotteSubListResponseData {
  sub_no: string;
  sub_tp: SubType;
}
