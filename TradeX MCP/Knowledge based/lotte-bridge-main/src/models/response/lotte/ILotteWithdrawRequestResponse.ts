import { ILotteCommonResponse } from './ILotteCommonResponse';
import { IParam } from '../../request/lotte/ILotteRequest';

export interface ILotteWithdrawRequestResponse extends ILotteCommonResponse {
  data_list: IParam[];
}
