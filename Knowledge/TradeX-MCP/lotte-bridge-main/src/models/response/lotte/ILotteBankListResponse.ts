import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteBankListResponse extends ILotteCommonResponse {
  data_list: ILotteBankListResponseData[];
}

export interface ILotteBankListResponseData {
  bank_code: string;
  bank_name: string;
  bank_account: string;
  bank_branch: string;
  bank_accountname: string;
}
