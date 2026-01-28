import { Models } from 'tradex-common';

export interface ICancelOrderNormalRequest extends Models.IDataRequest {
  name: string;
  subNumber: string;
  branchCode: string;
  orderNumber: string;
  accountNumber: string;
  deviceUniqueId: string;
  channel: string;
}

export interface ICancelOrderAdvancedRequest extends Models.IDataRequest {
  subNumber: string;
  advanceOrderDate: string;
  orderNumber: string;
  accountNumber: string;
  deviceUniqueId: string;
  channel: string;
}
