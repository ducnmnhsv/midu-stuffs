import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteRightHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteRightHistoryResponseData[];
}

export interface ILotteRightHistoryResponseData {
  listItems: ILotteRightHistoryResponseDataListItems[];
  next_data: string;
}

export interface ILotteRightHistoryResponseDataListItems {
  symbol: string;
  seq: string;
  base_date: string;
  status: string;
  base_rate: string;
  divd_rate: string;
  own_qtty: string;
  begin_date: string;
  end_date: string;
  issue_price: string;
  avail_qtty: string;
  req_qtty: string;
  req_amt: string;
  effect_date: string;
  effect_yn: string;
}
