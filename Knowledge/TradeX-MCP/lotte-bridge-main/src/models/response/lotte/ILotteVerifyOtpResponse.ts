import { IParam } from '../../request/lotte/ILotteRequest';
import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteVerifyOtpResponse extends ILotteCommonResponse {
  data_list: IParam[];
}
