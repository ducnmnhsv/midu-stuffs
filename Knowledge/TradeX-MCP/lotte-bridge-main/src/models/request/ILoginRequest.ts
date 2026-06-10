import { Models } from 'tradex-common';

export interface ILoginRequest extends Models.IDataRequest {
  username: string;
  password: string;
}
