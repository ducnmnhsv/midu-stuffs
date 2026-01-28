import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteLoanEstimatedFeeResponse extends ILotteCommonResponse {
  data_list: ILotteLoanEstimatedFeeResponseData[];
}

export interface ILotteLoanEstimatedFeeResponseData {
  adv_payment_fee: string;
}
