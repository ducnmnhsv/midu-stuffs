import { Models } from 'tradex-common';

export interface IEstAssetLoanInfoRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
}
