import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteRegisterBankAccountResponseData {
  scrt_err_msg: string;
}

export interface ILotteRegisterBankAccountResponse extends ILotteCommonResponse {
  data_list: ILotteRegisterBankAccountResponseData[];
}

export interface ILotteDeleteBankAccountResponseData {
  scrt_err_msg: string;
}

export interface ILotteDeleteBankAccountResponse extends ILotteCommonResponse {
  data_list: ILotteDeleteBankAccountResponseData[];
}
