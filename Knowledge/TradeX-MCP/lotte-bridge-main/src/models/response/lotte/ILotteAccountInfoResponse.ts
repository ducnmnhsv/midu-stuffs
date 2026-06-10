import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteAccountInfoResponse extends ILotteCommonResponse {
  data_list: ILotteAccountInfoData[];
}

export interface ILotteAccountInfoData {
  customer_name: string;
  identity_card: string;
  phone: string;
  email: string;
  address: string;
  manager: string;
}
