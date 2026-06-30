import { Models } from 'tradex-common';

export interface IRegisterSmartOtpRequest extends Models.IDataRequest {
  mode: string;
  otpKey?: string;
  deviceUniqueId?: string;
}