import { Models } from 'tradex-common';

export interface IAccountContractStatusRequest extends Models.IDataRequest {
  accountNumber: string;
}
