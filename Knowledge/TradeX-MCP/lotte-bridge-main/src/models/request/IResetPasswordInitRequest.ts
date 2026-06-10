import { Models } from 'tradex-common';

export interface IResetPasswordInitRequest extends Models.IDataRequest {
  accountNumber: string;
  phoneNumber: string;
  identifierNumber: string;
}
