import { Models } from 'tradex-common';

export interface IBankListRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
}
