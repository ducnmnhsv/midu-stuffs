import { Models } from 'tradex-common';

export interface IBuyableRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  bankCode: string;
  stockCode: string;
  marketType: string;
  securitiesType: string;
  orderPrice: string;
  orderQuantity: string;
  bankName: string;
}
