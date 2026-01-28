import { ILotteCommonResponse } from '../../response/lotte/ILotteCommonResponse';

export interface ILotteRightHistoryBaseResponse<T extends ILotteRightHistoryBaseItem> extends ILotteCommonResponse {
  data_list: Array<ILotteRightHistoryDataList<T>>;
}

export interface ILotteRightHistoryDataList<T> {
  listItems: T[];
  next_data: string;
}

export interface ILotteRightHistoryBaseItem {}
