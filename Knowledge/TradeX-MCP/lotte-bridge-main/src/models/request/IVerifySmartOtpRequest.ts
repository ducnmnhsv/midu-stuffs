import { Models } from 'tradex-common';

export interface IVerifySmartOtpRequest extends Models.IDataRequest {
  sotpKey: string;
  otpCode: string;
}
