import { Models } from 'tradex-common';

export interface ISubListRequest extends Models.IDataRequest {
  accountNumber: string;
}
