import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteChangeBrokerResponse extends ILotteCommonResponse {
  data_list: ILotteChangeBrokerData[];
}

export interface ILotteChangeBrokerData {
  seq_no: string;
}

