import { Models } from 'tradex-common';
import { OrderType, SellBuyType } from '../../constants/enum';

export interface INormalOrderRequest extends Models.IDataRequest {
  name: string;
  stockCode: string;
  subNumber: string;
  orderPrice: number;
  accountNumber: string;
  orderQuantity: number;
  securitiesType: string;
  sellBuyType: SellBuyType;
  bankAccount: string;
  bankName: string;
  bankCode: string;
  orderType: OrderType;
  deviceUniqueId: string;
  channel: string;
}

export interface IAdvancedOrderRequest extends Models.IDataRequest {
  bankCode: string;
  orderType: OrderType;
  stockCode: string;
  subNumber: string;
  orderPrice: number;
  bankAccount: string;
  phoneNumber: string;
  sellBuyType: SellBuyType;
  accountNumber: string;
  orderQuantity: number;
  securitiesType: string;
  advanceOrderDate: string;
  deviceUniqueId: string;
  channel: string;
}
