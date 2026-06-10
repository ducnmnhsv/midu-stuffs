import { IParam } from '../../request/lotte/ILotteRequest';
import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteChangePinResponse extends ILotteCommonResponse {
  data_list: IParam[];
}
