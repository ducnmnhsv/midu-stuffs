import { Models } from 'tradex-common';

export interface IVsdStatusAccountRequest extends Models.IDataRequest {
  accountNumber: string;
  subAccount: string;
}
