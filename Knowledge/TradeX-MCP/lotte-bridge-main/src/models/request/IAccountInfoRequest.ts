import { Models } from 'tradex-common';

export interface IAccountInfoRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
}
