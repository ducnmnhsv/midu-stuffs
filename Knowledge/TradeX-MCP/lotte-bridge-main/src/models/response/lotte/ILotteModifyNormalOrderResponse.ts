import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteModifyOrderNormalResponse extends ILotteCommonResponse {
  data_list: ILotteModifyOrderNormalResponseData[];
}

export interface ILotteModifyOrderNormalResponseData {
  new_ord_no: string;
  oms_ord_no: string;
  dummy_field: string;
}
