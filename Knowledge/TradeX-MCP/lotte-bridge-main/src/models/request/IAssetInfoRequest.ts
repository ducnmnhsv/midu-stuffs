import { Models } from 'tradex-common';

export interface IAssetInfoRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  bankCode: string;
  bankAccount: string;
}
