import { Models } from 'tradex-common';
import { SellBuyType } from '../../constants/enum';

export interface IOrderConfirmRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  orders: IOrderConfirm[];
}

export interface IOrderConfirm {
  orderDate: string;
  orderNumber: string;
}

export interface IOrderConfirmHistoryRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  fromDate: string;
  toDate: string;
  confirmStatus: string;
  sellBuyType: SellBuyType;
  marketType: string;
  stockCode: string;
  cancelType: string;
  nextKey: string;
  fetchCount: number;
}
