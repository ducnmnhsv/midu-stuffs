import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteNormalOrderResponse extends ILotteCommonResponse {
  data_list: ILotteNormalOrderResponseData[];
}

export interface ILotteNormalOrderResponseData {
  new_ord_no: string;
  oms_ord_no: string;
  stock_code: string;
  dummy_field: string;
}

export interface ILotteAdvancedOrderResponse extends ILotteCommonResponse {
  data_list: ILotteAdvancedOrderResponseData[];
}

export interface ILotteAdvancedOrderResponseData {
  new_ord_no: string;
  stock_code: string;
}
