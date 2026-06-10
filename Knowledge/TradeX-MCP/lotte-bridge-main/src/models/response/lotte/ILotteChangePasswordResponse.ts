import { IParam } from '../../request/lotte/ILotteRequest';
import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteChangePasswordResponse extends ILotteCommonResponse {
  data_list: IParam[];
}
