import { Models } from 'tradex-common';

export interface IMarginAccountRequest extends Models.IDataRequest {
  accountNumber: string;
  subNumber: string;
  symbolCode: string;
}
