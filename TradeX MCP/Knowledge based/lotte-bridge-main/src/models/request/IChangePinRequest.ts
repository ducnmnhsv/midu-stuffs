import { Models } from 'tradex-common';

export interface IChangePinRequest extends Models.IDataRequest {
  subNumber: string;
  newPassword: string;
  oldPassword: string;
  accountNumber: string;
}
