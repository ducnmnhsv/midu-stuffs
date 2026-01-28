import { Models } from 'tradex-common';

export interface IChangePasswordRequest extends Models.IDataRequest {
  username: string;
  newPassword: string;
  oldPassword: string;
}
