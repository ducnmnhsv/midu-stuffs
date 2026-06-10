import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteTransferCashResponse extends ILotteCommonResponse {
  data_list: ILotteTransferCashResponseData[];
}

export interface ILotteTransferCashResponseData {
  date: string;
  send_seq: string;
  recv_seq: string;
}
