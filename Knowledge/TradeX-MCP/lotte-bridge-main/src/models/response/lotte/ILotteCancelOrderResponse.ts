import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteCancelOrderNormalResponse extends ILotteCommonResponse {
  data_list: ILotteCancelNormalOrderResponseData[];
}

export interface ILotteCancelNormalOrderResponseData {
  new_ord_no: string;
  oms_ord_no: string;
  dummy_field: string;
}

export interface ILotteCancelOrderAdvancedResponse extends ILotteCommonResponse {
  data_list: ILotteCancelAdvancedOrderResponseData[];
}

export interface ILotteCancelAdvancedOrderResponseData {
  ord_no: string;
}
