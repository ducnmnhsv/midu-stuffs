import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteOddlotLatestResponse extends ILotteCommonResponse {
  data_list: ILotteOddlotLatestResponseData[];
}

export interface ILotteOddlotLatestResponseData {
  list: ILotteOddlotLatestResponseDataItem[];
}

export interface ILotteOddlotLatestResponseDataItem {
  code: string;
  vol: string;
  bid1: string;
  bid1Size: string;
  offer1: string;
  offer1Size: string;
  bid2: string;
  bid2Size: string;
  offer2: string;
  offer2Size: string;
  bid3: string;
  bid3Size: string;
  offer3: string;
  offer3Size: string;
  foreignBuyVol: string;
  foreignSellVol: string;
}
