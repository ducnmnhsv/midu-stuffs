import { Models } from 'tradex-common';
import { OrderType, SellBuyType } from '../../constants/enum';

export interface IModifyOrderNormalRequest extends Models.IDataRequest {
  bankCode?: string;
  bankName?: string;
  orderType?: OrderType;
  stockCode?: string;
  subNumber?: string;
  branchCode?: string;
  marketType?: string;
  orderPrice?: number;
  bankAccount?: string;
  orderNumber?: string;
  sellBuyType?: SellBuyType;
  accountNumber?: string;
  orderQuantity?: number;
  securitiesType?: string;
  deviceUniqueId?: string;
  channel?: string;
}
